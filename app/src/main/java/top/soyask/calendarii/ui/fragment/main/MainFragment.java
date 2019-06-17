package top.soyask.calendarii.ui.fragment.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.circularreveal.CircularRevealCompat;
import com.google.android.material.circularreveal.CircularRevealFrameLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import top.soyask.calendarii.R;
import top.soyask.calendarii.database.dao.ThingDao;
import top.soyask.calendarii.entity.Day;
import top.soyask.calendarii.entity.Thing;
import top.soyask.calendarii.ui.adapter.month.MonthFragmentAdapter;
import top.soyask.calendarii.ui.adapter.thing.ThingAdapter;
import top.soyask.calendarii.ui.fragment.about.AboutFragment;
import top.soyask.calendarii.ui.fragment.backup.BackupFragment;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;
import top.soyask.calendarii.ui.fragment.dialog.DateSelectDialog;
import top.soyask.calendarii.ui.fragment.event.AllThingsFragment;
import top.soyask.calendarii.ui.fragment.event.EditThingFragment;
import top.soyask.calendarii.ui.fragment.month.MonthFragment;
import top.soyask.calendarii.ui.fragment.setting.SettingFragment;
import top.soyask.calendarii.utils.MonthUtils;

import java.util.ArrayList;
import java.util.Calendar;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static top.soyask.calendarii.global.Global.MONTH_COUNT;
import static top.soyask.calendarii.global.Global.YEAR_START_REAL;


public class MainFragment extends BaseFragment
    implements ViewPager.OnPageChangeListener, View.OnClickListener, MonthFragment.OnDaySelectListener, EditThingFragment.OnAddListener, DateSelectDialog.DateSelectCallback {


    private Calendar mCalendar = Calendar.getInstance();
    private ViewPager mViewPager;
    private Day mSelectedDay;
    private ActionBar mActionBar;
    private MainReceiver mMainReceiver;
    private MenuItem mItemToday;
    private BottomSheetBehavior<FrameLayout> mBottomSheetBehavior;
    private CircularRevealFrameLayout mCollapseView;
    private View mRlLeftBottom;
    private FloatingActionButton mFabActions;

    public MainFragment() {
        super(R.layout.fragment_main);
    }

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupUI() {
        mContentView.setOnTouchListener((v, event) -> false);
        initSelectDay();
        setupToolbar();
        setupViewPager();
        setupEventList();

        mCollapseView = findViewById(R.id.collapse_view);
        mRlLeftBottom = findViewById(R.id.rl_leftbottom);
        mFabActions = findViewById(R.id.fab_actions);
    }

    private void initSelectDay() {
        mSelectedDay = MonthUtils.generateDay(mCalendar, ThingDao.getInstance(mHostActivity));
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        mActionBar = setToolbar(toolbar);
        setToolbarDate(mCalendar.get(YEAR), mCalendar.get(MONTH) + 1);
    }

    private void setupViewPager() {
        int item = getCurrentMonth();
        mViewPager = findViewById(R.id.vp);
        MonthFragmentAdapter monthFragmentAdapter = new MonthFragmentAdapter(getChildFragmentManager(), this);
        mViewPager.setAdapter(monthFragmentAdapter);
        mViewPager.setCurrentItem(item);
        mViewPager.addOnPageChangeListener(this);
    }

    /**
     * 包含事件、日程、纪念日
     */
    private void setupEventList() {
        Toolbar toolbarBottomSheet = findViewById(R.id.toolbar_bottom_sheet);
        View bottomBackground = findViewById(R.id.bottom_background);
        RecyclerView recyclerView = findViewById(R.id.rv_event_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(mHostActivity,RecyclerView.VERTICAL,false));
        ArrayList<Thing> things = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            things.add(new Thing());
        }
        recyclerView.setAdapter(new ThingAdapter(things,null));
        mBottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override public void onStateChanged(@NonNull View bottomSheet, int newState) { }

            @Override public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                toolbarBottomSheet.setAlpha(slideOffset * 1f);
                bottomBackground.setAlpha(slideOffset * 1f);
                recyclerView.setTranslationY(slideOffset * toolbarBottomSheet.getHeight());
            }
        });
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private int getCurrentMonth() {
        return (mCalendar.get(YEAR) - MonthFragmentAdapter.YEAR_START) * 12 + mCalendar.get(MONTH);
    }

    private void setToolbarDate(int year, int month) {
        String title = getString(R.string.xx_year_xx_month, year, month);
        mActionBar.setTitle(title);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        mItemToday = menu.getItem(0);
        toggleItemToday(mViewPager.getCurrentItem());
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_today:
                skipToday();
                break;
            case R.id.menu_all_event:
                AllThingsFragment allThingsFragment = AllThingsFragment.newInstance(null);
                addFragment(allThingsFragment);
                break;
            case R.id.menu_select:
                showSelectDialog();
                break;
            case R.id.menu_score:
                score();
                break;
            case R.id.menu_about:
                AboutFragment aboutFragment = AboutFragment.newInstance();
                addFragment(aboutFragment);
                break;
            case R.id.menu_setting:
                SettingFragment settingFragment = SettingFragment.newInstance();
                addFragment(settingFragment);
                break;
            case R.id.menu_backup:
                BackupFragment backupFragment = BackupFragment.newInstance();
                addFragment(backupFragment);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void skipToday() {
        skipToOneDay(mCalendar.get(YEAR), mCalendar.get(MONTH) + 1, mCalendar.get(DAY_OF_MONTH));
    }


    private void showSelectDialog() {
        DateSelectDialog dateSelectDialog = DateSelectDialog.newInstance(mSelectedDay.getYear(), mSelectedDay.getMonth(), mSelectedDay.getDayOfMonth());
        dateSelectDialog.show(getChildFragmentManager(), "");
        dateSelectDialog.setDateSelectCallback(this);
    }

    private void score() {
        Uri uri = Uri.parse("market://details?id=" + mHostActivity.getPackageName());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(mHostActivity.getPackageManager()) != null) { //可以接收
            startActivity(intent);
        } else {
            Toast.makeText(mHostActivity, R.string.no_market, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        int year = position / MONTH_COUNT + YEAR_START_REAL;
        int month = position % MONTH_COUNT + 1;
        setToolbarDate(year, month);
        toggleItemToday(position);
    }

    private void toggleItemToday(int position) {
        if (getCurrentMonth() == position) {
            if (mItemToday != null && mItemToday.isVisible()) {
                mItemToday.setVisible(false);
            }
        } else {
            if (mItemToday != null && !mItemToday.isVisible()) {
                mItemToday.setVisible(true);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onClick(View v) {
        EditThingFragment editThingFragment = EditThingFragment.newInstance(mSelectedDay, null);
        editThingFragment.setOnAddListener(this);
        addFragment(editThingFragment);
    }

    @Override
    public void onSelected(Day day) {
        this.mSelectedDay = day;
        calculateDelta_T();
        if (day.getDayOfMonth() % 2 == 0) {
            showCollapseViewWithAnim();
        }else {
            hideCollapseViewWithAnim();
        }
    }

    @Override
    public void skipToNextMonth(int dayOfMonth) {
        skipToOneDay(1, dayOfMonth);
    }

    @Override
    public void skipToPrevMonth(int dayOfMonth) {
        skipToOneDay(-1, dayOfMonth);
    }

    // 计算所选的天数到今天的时间差
    private void calculateDelta_T() {
        Calendar selectDay = getSelectedCalendar();
        Calendar today = getTodayCalendar();

        long todayTime = today.getTime().getTime() / 86400000;
        long time = selectDay.getTime().getTime() / 86400000;
        Long l = time - todayTime;
        int dayCount = l.intValue();
//        if (dayCount > 0) {
//            mTvDayCount.setText(getString(R.string.till_xx_days_ago, dayCount));
//            mTvDayCountM.setText(getString(R.string.xx_later, dayCount));
//        } else if (dayCount < 0) {
//            mTvDayCount.setText(getString(R.string.it_has_been_xx_days, -dayCount));
//            mTvDayCountM.setText(getString(R.string.xx_before, -dayCount));
//        } else {
//            mTvDayCount.setText(R.string.today_things);
//            mTvDayCountM.setText(null);
//        }
    }

    @NonNull
    private Calendar getSelectedCalendar() {
        return getCalendarBy(mSelectedDay.getYear(), mSelectedDay.getMonth() - 1, mSelectedDay.getDayOfMonth());
    }

    @NonNull
    private Calendar getTodayCalendar() {
        return getCalendarBy(mCalendar.get(YEAR), mCalendar.get(MONTH), mCalendar.get(DAY_OF_MONTH));
    }

    @NonNull
    private Calendar getCalendarBy(int year, int month, int dayOfMonth) {
        Calendar today = Calendar.getInstance();
        today.set(YEAR, year);
        today.set(MONTH, month);
        today.set(DAY_OF_MONTH, dayOfMonth);
        return today;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupReceiver();
    }


    private void setupReceiver() {
        mMainReceiver = new MainReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MonthFragment.ADD_EVENT);
        filter.addAction(MonthFragment.UPDATE_EVENT);
        filter.addAction(MonthFragment.DELETE_EVENT);
        mHostActivity.registerReceiver(mMainReceiver, filter);
    }

    @Override
    public void onDestroy() {
        mHostActivity.unregisterReceiver(mMainReceiver);
        super.onDestroy();
    }

    @Override
    public void onSelectCancel() {
    }

    @Override
    public void onDismiss() {
    }

    @Override
    public void onValueChange(int year, int month, int day) {
    }

    @Override
    public void onSelectConfirm(int year, int month, int day) {
        skipToOneDay(year, month, day);
    }

    private void skipToOneDay(int offset, int dayOfMonth) {
        int position = mViewPager.getCurrentItem() + offset;
        int year = position / MONTH_COUNT + YEAR_START_REAL;
        int month = position % MONTH_COUNT + 1;
        mViewPager.setCurrentItem(position);
        Intent intent = new Intent(MonthFragment.SKIP);
        intent.putExtra("year", year);
        intent.putExtra("month", month);
        intent.putExtra("day", dayOfMonth);
        mHostActivity.sendBroadcast(intent);
    }

    private void skipToOneDay(int year, int month, int day) {
        int position = (year - MonthFragmentAdapter.YEAR_START) * 12 + month - 1;
        skipToOneDay(position - mViewPager.getCurrentItem(), day);
    }

    @Override
    public void onAdd() {
        showSnackbar("成功添加了新事件");
    }


    private void showCollapseView(){
        mCollapseView.setVisibility(View.VISIBLE);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mRlLeftBottom.setVisibility(View.GONE);
        mFabActions.setVisibility(View.GONE);
        new Handler().post(()-> mBottomSheetBehavior.setHideable(false));
    }

    private void showLeftBottomView(){
        mCollapseView.setVisibility(View.INVISIBLE);
        mBottomSheetBehavior.setHideable(true);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mRlLeftBottom.setVisibility(View.VISIBLE);
        mFabActions.setVisibility(View.VISIBLE);
    }

    private void showCollapseViewWithAnim(){
        Animator circularReveal = CircularRevealCompat.createCircularReveal(
            mCollapseView, 0, mCollapseView.getHeight(), mCollapseView.getHeight(),mCollapseView.getWidth());
        int translationX = -30;
        ObjectAnimator translation =
            ObjectAnimator.ofFloat(mCollapseView, "translationX", translationX, 0f);
        AnimatorSet set = new AnimatorSet()
            .setDuration(500);
        set.setTarget(mCollapseView);
        set.playTogether(circularReveal,translation);
        set.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                hideLeftBottomViewWithAnim();
                mCollapseView.setVisibility(View.VISIBLE);
            }
        });
        set.start();
    }

    private void hideCollapseViewWithAnim(){
        Animator circularReveal = CircularRevealCompat.createCircularReveal(
            mCollapseView, 0, mCollapseView.getHeight(),mCollapseView.getWidth(), mCollapseView.getHeight());
        int translationX = -30;
        ObjectAnimator translation =
            ObjectAnimator.ofFloat(mCollapseView, "translationX", 0f, translationX);
        AnimatorSet set = new AnimatorSet()
            .setDuration(500);
        set.setTarget(mCollapseView);
        set.playTogether(circularReveal,translation);
        set.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                showLeftBottomViewWithAnim();
                mCollapseView.setVisibility(View.INVISIBLE);
            }
        });
        set.start();
    }

    private void showLeftBottomViewWithAnim(){
        Animation animation = AnimationUtils.loadAnimation(mHostActivity, R.anim.fade_in);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {
                mRlLeftBottom.setVisibility(View.VISIBLE);
            }

            @Override public void onAnimationEnd(Animation animation) {

            }

            @Override public void onAnimationRepeat(Animation animation) {

            }
        });

        mRlLeftBottom.startAnimation(animation);
    }

    private void hideLeftBottomViewWithAnim(){
        Animation animation = AnimationUtils.loadAnimation(mHostActivity, R.anim.out_slide);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {
            }

            @Override public void onAnimationEnd(Animation animation) {
                mRlLeftBottom.setVisibility(View.GONE);
            }

            @Override public void onAnimationRepeat(Animation animation) {
            }
        });

        mRlLeftBottom.startAnimation(animation);
    }

    public class MainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            onSelected(mSelectedDay);
        }
    }

}
