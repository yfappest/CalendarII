package top.soyask.calendarii;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;

import top.soyask.calendarii.global.Global;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.ui.fragment.MainFragment;

public class MainActivity extends AppCompatActivity {

    private static final int[] THEMES = {
            R.style.AppTheme,
            R.style.AppTheme_Green,
            R.style.AppTheme_Pink,
            R.style.AppTheme_Teal,
            R.style.AppTheme_Blue,
            R.style.AppTheme_Red,
            R.style.AppTheme_Purple,
            R.style.AppTheme_Black,
            R.style.AppTheme_Red
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Setting.loadSetting(this);
        setupTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            init();
        }
    }

    private void setupTheme() {
        setTheme(THEMES[ Setting.theme ]);
    }

    @Deprecated
    private void loadSetting(){
        SharedPreferences setting = getSharedPreferences("setting", MODE_PRIVATE);
        Setting.theme = setting.getInt(Global.SETTING_THEME, 0);
        Setting.date_offset = setting.getInt(Global.SETTING_DATE_OFFSET, 0);
    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    private void init() {
        MainFragment mainFragment = MainFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment, mainFragment)
                .commit();
    }


}
