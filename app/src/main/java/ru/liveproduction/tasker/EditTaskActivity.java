package ru.liveproduction.tasker;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.awt.font.TextAttribute;

public class EditTaskActivity extends AppCompatActivity {

    private BroadcastReceiver broadcastReceiver;
    private boolean createTask = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_edit_task);

        ((CheckBox) findViewById(R.id.repeat)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    findViewById(R.id.repeatLayout).setVisibility(View.VISIBLE);
                else
                    findViewById(R.id.repeatLayout).setVisibility(View.GONE);
            }
        });

        ((Spinner) findViewById(R.id.unitOfRepeat)).setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[]{getResources().getString(R.string.textMinutes_layout_edit_task), getResources().getString(R.string.textHours_layout_edit_task), getResources().getString(R.string.textDays_layout_edit_task), getResources().getString(R.string.textWeeks_layout_edit_task), getResources().getString(R.string.textMonths_layout_edit_task), getResources().getString(R.string.textYears_layout_edit_task)}));
        ((Spinner) findViewById(R.id.typeOfAction)).setAdapter((new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[]{"+", "-"})));

        String name = getIntent().getStringExtra("objectName");
        if (name != null) {
            TaskLL task = ApplicationLL.manager.get(name);
            if (task != null) {
                createTask = false;
                setTask(task);
                if (broadcastReceiver == null)
                    broadcastReceiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            if (!createTask) {
                                String name = getIntent().getStringExtra("objectName");
                                if (name != null) {
                                    final TaskLL taskLL = ApplicationLL.manager.get(name);
                                    if (taskLL != null && taskLL.getName().toLowerCase().equals(((EditText) findViewById(R.id.nameOfTask)).getText().toString().toLowerCase()))
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ((TextView) findViewById(R.id.count)).setText(String.valueOf(taskLL.getCount()));
                                            }
                                        });
                                }
                            }
                        }
                    };
            }
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String name = getIntent().getStringExtra("objectName");
        if (name != null) {
            TaskLL task = ApplicationLL.manager.get(name);
            if (task != null) {
                createTask = false;
                setTask(task);
                if (broadcastReceiver == null)
                    broadcastReceiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            if (!createTask) {
                                String name = getIntent().getStringExtra("objectName");
                                if (name != null) {
                                    final TaskLL taskLL = ApplicationLL.manager.get(name);
                                    if (taskLL != null && taskLL.getName().toLowerCase().equals(((EditText) findViewById(R.id.nameOfTask)).getText().toString().toLowerCase()))
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ((TextView) findViewById(R.id.count)).setText(String.valueOf(taskLL.getCount()));
                                            }
                                        });
                                }
                            }
                        }
                    };
            }
        } else {
            setContentView(R.layout.layout_edit_task);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!createTask) {
            getMenuInflater().inflate(R.menu.menu_toolbar_existing_task, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.deleteButton) {
            String name = getIntent().getStringExtra("objectName");
            if (name != null) {
                Intent intent = new Intent();
                intent.putExtra("objectName", name);
                setResult(-2, intent);

                ApplicationLL.manager.remove(name);
                finish();
            }
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (broadcastReceiver != null)
            registerReceiver(broadcastReceiver, new IntentFilter("ru.liveproduction.tasker.update.intent.UpdateTask"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (broadcastReceiver != null)
            unregisterReceiver(broadcastReceiver);
    }

    public void setTask(final TaskLL task) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((EditText) findViewById(R.id.nameOfTask)).setText(task.getName());
                findViewById(R.id.nameOfTask).setEnabled(false);
                ((TextView) findViewById(R.id.count)).setText(String.valueOf(task.getCount()));
                ((CheckBox) findViewById(R.id.repeat)).setChecked(task.isRepeat());
                if (task.isRepeat()) {
                    ((EditText) findViewById(R.id.countOfRepeat)).setText(String.valueOf(task.getCountOfRepeat()));
                    ((Spinner) findViewById(R.id.unitOfRepeat)).setSelection(TaskLL.indexOfUnit(task.getTypeOfRepeat()) - 1);
                    ((Spinner) findViewById(R.id.typeOfAction)).setSelection(task.isUsePlus() ? 0 : 1);
                    ((EditText) findViewById(R.id.countOfAction)).setText(String.valueOf(task.getCountOfAction()));
                    ((CheckBox) findViewById(R.id.usingAlarm)).setChecked(task.isCreateNotification());
                }

                ((EditText) findViewById(R.id.maxCount)).setText(String.valueOf(task.getMaxCount() > 0 ? task.getMaxCount() : 0));
            }
        });
    }


    public void add1(View view) {
        final TextView text = findViewById(R.id.count);
        int max = Integer.valueOf(((EditText) findViewById(R.id.maxCount)).getText().toString());
        int result = Integer.valueOf(text.getText().toString()) + 1;
        if (max > 0)
            result = Math.min(max, result);

        final int finalResult = result;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(String.valueOf(finalResult));
            }
        });
    }

    public void add5(View view) {
        final TextView text = findViewById(R.id.count);
        int max = Integer.valueOf(((EditText) findViewById(R.id.maxCount)).getText().toString());
        int result = Integer.valueOf(text.getText().toString()) + 5;
        if (max > 0)
            result = Math.min(max, result);

        final int finalResult = result;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(String.valueOf(finalResult));
            }
        });
    }

    public void add10(View view) {
        final TextView text = findViewById(R.id.count);
        int max = Integer.valueOf(((EditText) findViewById(R.id.maxCount)).getText().toString());
        int result = Integer.valueOf(text.getText().toString()) + 10;
        if (max > 0)
            result = Math.min(max, result);

        final int finalResult = result;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(String.valueOf(finalResult));
            }
        });
    }

    public void add(View view) {
        final Context context = this;

        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.textAdd_dialog_title);
        dialog.setView(editText);
        dialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final int added;
                try {
                    added = Integer.valueOf(editText.getText().toString());
                    final TextView text = findViewById(R.id.count);
                    int max = Integer.valueOf(((EditText) findViewById(R.id.maxCount)).getText().toString());
                    int result = Integer.valueOf(text.getText().toString()) + added;
                    if (max > 0)
                        result = Math.min(result, max);
                    final int finalResult = result;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            text.setText(String.valueOf(finalResult));
                        }
                    });
                } catch (Exception ignore) {
                    Toast.makeText(context, R.string.textWrongNumber_error_toast, Toast.LENGTH_SHORT).show();
                }
                dialogInterface.dismiss();
            }
        });

        dialog.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
    }

    public void sub1(View view) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView text = findViewById(R.id.count);
                text.setText(String.valueOf(Math.max(0, Integer.valueOf(text.getText().toString()) - 1)));
            }
        });
    }

    public void sub5(View view) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView text = findViewById(R.id.count);
                text.setText(String.valueOf(Math.max(0, Integer.valueOf(text.getText().toString()) - 5)));
            }
        });
    }
    public void sub10(View view) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView text = findViewById(R.id.count);
                text.setText(String.valueOf(Math.max(0, Integer.valueOf(text.getText().toString()) - 10)));
            }
        });
    }

    public void sub(View view) {
        final Context context = this;

        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.textSub_dialog_title);
        dialog.setView(editText);
        dialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final int sub;
                try {
                    sub = Integer.valueOf(editText.getText().toString());
                    final TextView text = findViewById(R.id.count);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            text.setText(String.valueOf(Math.max(0, Integer.valueOf(text.getText().toString()) - sub)));
                        }
                    });
                } catch (Exception ignore) {
                    Toast.makeText(context, R.string.textWrongNumber_error_toast, Toast.LENGTH_SHORT).show();
                }
                dialogInterface.dismiss();
            }
        });

        dialog.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
    }



    public String getValue(int id) {
        return ((TextView)findViewById(id)).getText().toString();
    }

    public long getLong(int id) {
        try {
            return Long.valueOf(getValue(id));
        }catch (Exception ignore){
            return 0;
        }
    }

    public boolean getBoolean(int id) {
        return ((CheckBox) findViewById(id)).isChecked();
    }



    public void ok(View view){
        String name = getValue(R.id.nameOfTask);
        long count = getLong(R.id.count);

        if (name.length() > 0) {
            TaskLL task = new TaskLL(name, count);
            task.setRepeat(getBoolean(R.id.repeat));
            task.setMaxCount(getLong(R.id.maxCount));
            if (task.isRepeat()) {
                task.setCountOfRepeat((int) getLong(R.id.countOfRepeat));

                TaskLL.REPEATED_INTERVALS interval = null;
                switch (((Spinner) findViewById(R.id.unitOfRepeat)).getSelectedItemPosition()) {
                    case 0:
                        interval = TaskLL.REPEATED_INTERVALS.MINUTE;
                        break;
                    case 1:
                        interval = TaskLL.REPEATED_INTERVALS.HOUR;
                        break;
                    case 2:
                        interval = TaskLL.REPEATED_INTERVALS.DAY;
                        break;
                    case 3:
                        interval = TaskLL.REPEATED_INTERVALS.WEEK;
                        break;
                    case 4:
                        interval = TaskLL.REPEATED_INTERVALS.MONTH;
                        break;
                    case 5:
                        interval = TaskLL.REPEATED_INTERVALS.YEAR;
                        break;
                }
                task.setTypeOfRepeat(interval);
                task.setUsePlus(((Spinner) findViewById(R.id.typeOfAction)).getSelectedItemPosition() == 0);
                task.setCountOfAction(getLong(R.id.countOfAction));
                task.setCreateNotification(getBoolean(R.id.usingAlarm));
            }

            long max = getLong(R.id.maxCount);
            if (max > 0)
                task.setMaxCount(max);


            if (task.isRepeat()) {
                if (!createTask)
                    ((AlarmManager) getSystemService(Context.ALARM_SERVICE)).cancel(AlarmManagerLL.createIntentForNotification(this, task));

                task.setStartTime(System.currentTimeMillis());

                ((AlarmManager) getSystemService(Context.ALARM_SERVICE)).setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + task.getTimeToCloserRepeatInMs(),
                        task.getTimeForRepeatInMs(),
                        AlarmManagerLL.createIntentForNotification(this, task)
                );
            }

            ApplicationLL.manager.update(task);

            Intent data = new Intent();
            data.putExtra("objectName", task.getName().toLowerCase());
            setResult(RESULT_OK, data);
            finish();
        }
        else {
            Toast.makeText(this, R.string.textNeedName_error_toast, Toast.LENGTH_SHORT).show();
        }
    }

    public void cancel(View view) {
        if (createTask)
            setResult(RESULT_CANCELED);
        else {
            Intent intent = new Intent();
            intent.putExtra("objectName", getIntent().getStringExtra("objectName"));
            setResult(-3, intent);
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        cancel(null);
    }

    @Override
    protected void onDestroy() {
        if (!getIntent().getBooleanExtra("startMain", false))
            ApplicationLL.manager.save(this);
        super.onDestroy();
    }
}
