package ru.liveproduction.tasker;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EditTaskActivity extends AppCompatActivity {

    private BroadcastReceiver broadcastReceiver;
    private boolean createTask = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(ApplicationLL.LOG_TAG, "EditTaskActivity -> onStart()");

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

        ((Spinner) findViewById(R.id.unitOfRepeat)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > 0) {
                    findViewById(R.id.layout_seconds).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.layout_seconds).setVisibility(View.GONE);
                }

                if (i > 1) {
                    findViewById(R.id.layout_minutes).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.layout_minutes).setVisibility(View.GONE);
                }

                if (i > 2) {
                    findViewById(R.id.layout_hours).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.layout_hours).setVisibility(View.GONE);
                }

                if (i == 4) {
                    findViewById(R.id.layout_dayOfWeek).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.layout_dayOfWeek).setVisibility(View.GONE);
                }

                if (i > 4) {
                    findViewById(R.id.layout_day).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.layout_day).setVisibility(View.GONE);
                }

                if (i > 5) {
                    findViewById(R.id.layout_month).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.layout_month).setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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
            } else {
                Intent intent1 = new Intent();
                intent1.putExtra("objectName", name);
                setResult(-2, intent1);

                finish();
            }
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(ApplicationLL.LOG_TAG, "EditTaskActivity -> onNewIntent()");

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
            } else {
                Intent intent1 = new Intent();
                intent1.putExtra("objectName", name);
                setResult(-2, intent1);

                finish();
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
        Log.d(ApplicationLL.LOG_TAG, "EditTaskActivity -> onStart()");

        if (broadcastReceiver != null)
            registerReceiver(broadcastReceiver, new IntentFilter("ru.liveproduction.tasker.update.intent.UpdateTask"));

        String name = getIntent().getStringExtra("objectName");
        if (name != null) {
            if (ApplicationLL.manager.getNeedToUpdate().contains(name)) {
                final TaskLL taskLL = ApplicationLL.manager.use(name);
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

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(ApplicationLL.LOG_TAG, "EditTaskActivity -> onStop()");

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
                    ((Spinner) findViewById(R.id.unitOfRepeat)).setSelection(task.getTypeOfRepeat());
                    ((Spinner) findViewById(R.id.typeOfAction)).setSelection(task.getCountOfAction() > 0 ? 0 : 1);
                    ((EditText) findViewById(R.id.countOfAction)).setText(String.valueOf(Math.abs(task.getCountOfAction())));
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
        editText.setHint(R.string.textNumber_dialog_edit_text_hint);
        editText.setLeft(16);
        editText.setRight(16);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.textAdd_dialog_title);
        dialog.setView(editText);
        dialog.setPositiveButton(R.string.layout_edit_task_ok_button, new DialogInterface.OnClickListener() {
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

        dialog.setNegativeButton(R.string.layout_edit_task_cancel_button, new DialogInterface.OnClickListener() {
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
        editText.setHint(R.string.textNumber_dialog_edit_text_hint);
        editText.setLeft(16);
        editText.setRight(16);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.textSub_dialog_title);
        dialog.setView(editText);
        dialog.setPositiveButton(R.string.layout_edit_task_ok_button, new DialogInterface.OnClickListener() {
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

        dialog.setNegativeButton(R.string.layout_edit_task_cancel_button, new DialogInterface.OnClickListener() {
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


    private void sendIntentToService(String action, String objectName) {
        Intent actionAdd = new Intent(this, AlarmManagerService.class);
        actionAdd.setAction(action);
        actionAdd.putExtra("objectName", objectName);

        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };

        bindService(actionAdd, serviceConnection, 0);
        unbindService(serviceConnection);
    }


    public void ok(View view){
        Log.d(ApplicationLL.LOG_TAG, "EditTaskActivity -> ok(); //Send task to db");
        String name = getValue(R.id.nameOfTask);
        long count = getLong(R.id.count);

        if (name.length() > 0) {
            boolean repeat = getBoolean(R.id.repeat);
            long max = getLong(R.id.maxCount);
            boolean createNotification = getBoolean(R.id.usingAlarm);

            TaskLL task;

            if (repeat) {
                int countOfRepeat = (int) getLong(R.id.countOfRepeat);
                int unitOfRepeat = ((Spinner) findViewById(R.id.unitOfRepeat)).getSelectedItemPosition();
                long countOfAction = getLong(R.id.countOfAction);
                if (((Spinner) findViewById(R.id.typeOfAction)).getSelectedItemPosition() == 1)
                    countOfAction = -countOfAction;

                ClockLL clock = new ClockLL();

                long seconds = getLong(R.id.seconds);
                seconds = seconds > 0 && seconds < 61 ? seconds % 60 : clock.getSeconds();
                long minutes = getLong(R.id.minutes);
                minutes = minutes > 0 && minutes < 61 ? minutes % 60: clock.getMinutes();
                long hours = getLong(R.id.hours);
                hours = hours > 0 && hours < 25 ? hours % 24: clock.getHour();
                long dayOfWeek = getLong(R.id.dayOfWeek);
                dayOfWeek = dayOfWeek > 0 && dayOfWeek < 8 ? dayOfWeek : clock.getDayOfWeek();
                long month = getLong(R.id.month);
                month = month > 0 && month < 13 ? month : clock.getMonth();
                long day = getLong(R.id.days);
                day = day > 0 && day < ClockLL.getDaysOfMonth(clock.getYear(), month) + 1 ? day : clock.getDay();

                long tmpDayOfWeek = clock.getDayOfWeek();
                while (tmpDayOfWeek != dayOfWeek) {
                    day--;
                    tmpDayOfWeek--;
                    if (tmpDayOfWeek < 1) {
                        tmpDayOfWeek = 7;
                    } else if (tmpDayOfWeek > 8) {
                        tmpDayOfWeek = 1;
                    }
                }


                switch (unitOfRepeat) {
                    case 1:
                        clock = new ClockLL(clock.getYear(), clock.getMonth(), clock.getDay(), clock.getHour(), clock.getMinutes(), seconds, clock.getMilliseconds());
                        break;
                    case 2:
                        clock = new ClockLL(clock.getYear(), clock.getMonth(), clock.getDay(), clock.getHour(), minutes, seconds, clock.getMilliseconds());
                        break;
                    case 3:
                        clock = new ClockLL(clock.getYear(), clock.getMonth(), clock.getDay(), hours, minutes, seconds, clock.getMilliseconds());
                        break;
                    case 4:
                        clock = new ClockLL(clock.getYear(), clock.getMonth(), day, hours, minutes, seconds, clock.getMilliseconds());
                        break;
                    case 5:
                        clock = new ClockLL(clock.getYear(), clock.getMonth(), day, hours, minutes, seconds, clock.getMilliseconds());
                        break;
                    case 6:
                        clock = new ClockLL(clock.getYear(), month, day, hours, minutes, seconds, clock.getMilliseconds());
                        break;
                }

                task = new TaskLL(name, count, Math.max(max, 0), repeat, countOfRepeat, unitOfRepeat, countOfAction, createNotification, createTask ? clock : ApplicationLL.manager.get(getIntent().getStringExtra("objectName")).getStartTime());
            } else {
                task = new TaskLL(name, count, Math.max(max, 0), false, 0, 0, 0, createNotification, createTask ? new ClockLL() : ApplicationLL.manager.get(getIntent().getStringExtra("objectName")).getStartTime());
            }

            ApplicationLL.manager.update(task);

            sendIntentToService(AlarmManagerService.COMMAND_ADD, task.getName().toLowerCase());

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
