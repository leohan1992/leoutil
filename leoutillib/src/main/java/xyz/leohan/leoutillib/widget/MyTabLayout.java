package xyz.leohan.leoutillib.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.List;

/**
 * Created by Leo on 2016/2/19.
 */
public class MyTabLayout extends RelativeLayout {
    private android.support.design.widget.TabLayout tabLayout;
    private ViewPager viewPager;
    private Context mContext;


    public MyTabLayout(Context context) {
        this(context, null);
    }

    public MyTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView(context);
    }

    /**
     * 动态布局代码
     * @param context 上下文
     */
    private void initView(Context context) {
        tabLayout = new android.support.design.widget.TabLayout(context);
        @android.support.annotation.IdRes int tabLayoutId = 1;
        @android.support.annotation.IdRes int viewPagerId = 2;
        tabLayout.setId(tabLayoutId);
        LayoutParams tabLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tabLayout.setLayoutParams(tabLayoutParams);
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#ffffff"));
        tabLayout.setTabGravity(android.support.design.widget.TabLayout.GRAVITY_CENTER);
        viewPager = new ViewPager(context);
        viewPager.setId(viewPagerId);
        LayoutParams viewPagerLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        viewPagerLayoutParams.addRule(BELOW, tabLayout.getId());
        viewPager.setLayoutParams(viewPagerLayoutParams);
        addView(tabLayout);
        addView(viewPager);

    }

    /**
     * 此方法为初始化方法
     * @param fm FragmentManager
     * @param mTitles 标题数组
     * @param mFragments fragmentList
     */
    public void initData(FragmentManager fm, String[] mTitles, List<Fragment> mFragments) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(fm, mTitles, mFragments);
        viewPager.setAdapter(adapter);
        // 设置ViewPager最大缓存的页面个数
        viewPager.setOffscreenPageLimit(5);
        // 给ViewPager添加页面动态监听器（为了让Toolbar中的Title可以变化相应的Tab的标题）

        tabLayout.setTabMode(android.support.design.widget.TabLayout.MODE_SCROLLABLE);
        // 将TabLayout和ViewPager进行关联，让两者联动起来
        tabLayout.setupWithViewPager(viewPager);
        // 设置Tablayout的Tab显示ViewPager的适配器中的getPageTitle函数获取到的标题
        tabLayout.setTabsFromPagerAdapter(adapter);

    }

    /**
     * 设置选中下划线的颜色
     * @param color 颜色
     */
    public void setSelectedTabIndicatorColor(int color) {
        tabLayout.setSelectedTabIndicatorColor(color);
    }

    /**
     * 这是滑动栏的颜色
     * @param color 颜色
     */
    public void setTabBarColor(int color) {
        tabLayout.setBackgroundColor(color);
    }

    /**
     * 设置字体颜色
     * @param normalColor 正常颜色
     * @param selectColor 选中颜色
     */
    public void setTextColor(int normalColor, int selectColor) {
        tabLayout.setTabTextColors(normalColor, selectColor);
    }

    /**
     * 设置滑动监听
     * @param listener ViewPager.OnPageChangeListener
     */
    public void addOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        viewPager.addOnPageChangeListener(listener);
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private String[] mTitles;
        private List<Fragment> mFragments;

        public ViewPagerAdapter(FragmentManager fm, String[] mTitles, List<Fragment> mFragments) {
            super(fm);
            this.mTitles = mTitles;
            this.mFragments = mFragments;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments == null ? 0 : mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }
    }

}
