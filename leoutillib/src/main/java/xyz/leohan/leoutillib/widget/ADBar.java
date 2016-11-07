package xyz.leohan.leoutillib.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Leo on 2016/2/15.
 * 主要实现广告轮播效果
 * 使用时按照一般控件使用
 * 通过setViewData()方法加载视图
 * 通过setTitleData()方法设置标题
 * 通过setOnPageClickListener()方法设置页面的点击事件
 */
public class ADBar extends RelativeLayout {
    private ViewPager mViewPager;
    private ADBarAdapter mAdapter = null;
    private TextView title;//广告标题
    private ArrayList<String> titleData = new ArrayList<>();//标题集合
    private onPageClickListener mListener;//点击监听
    private boolean isAutoRoll = true;//是否自动切换,默认自动切换
    private static final long delayTime = 2500;//自动切换间隔时间
    private Bitmap selectPoint = null;
    private Bitmap normalPoint = null;
    private Context mContext;
    private ArrayList<ImageView> points = new ArrayList<>();
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //用于实现自动切换
            super.handleMessage(msg);
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
            AutoRoll();
        }
    };

    public ADBar(Context context) {
        this(context, null);
    }

    public ADBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public ADBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        selectPoint = getBitmap(Color.parseColor("#aaFFFFFF"));
        normalPoint = getBitmap(Color.parseColor("#55000000"));
        //动态布局
        //ViewPager设置
        mViewPager = new ViewPager(context);
        ViewPager.LayoutParams viewPagerLayoutParams = new ViewPager.LayoutParams();
        viewPagerLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        viewPagerLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        mViewPager.setLayoutParams(viewPagerLayoutParams);
        addView(mViewPager);
        //标题设置
        title = new TextView(context);
        title.setTextSize(16);
        title.setTextColor(Color.parseColor("#ffffff"));
        LayoutParams titleLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleLayoutParams.addRule(ALIGN_PARENT_BOTTOM);
        titleLayoutParams.setMargins(5, 0, 0, 5);
        title.setLayoutParams(titleLayoutParams);
        addView(title);

    }

    /**
     * 资源适配器
     *
     * @param <T>
     */
    public class ADBarAdapter<T> extends PagerAdapter {
        private ArrayList<T> mData;

        protected ADBarAdapter(ArrayList<T> data) {
            this.mData = data;
        }


        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view = (View) mData.get(position % mData.size());
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            view.setLayoutParams(lp);
            container.addView(view);
            view.setOnClickListener(new OnClickListener() {//为每个view添加点击事件
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onPageClick(v, position % mData.size());//使用回调实现点击事件监听
                    }
                }
            });
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        /**
         * 取得资源对象
         *
         * @return 当前显示View的对象
         */
        public Object getItem() {
            return mData.get(mViewPager.getCurrentItem() % mAdapter.getDataCount());
        }

        /**
         * 取得数据个数
         *
         * @return adapter中的数据个数
         */
        public int getDataCount() {
            return mData == null ? 0 : mData.size();
        }
    }

    /**
     * 设置标题文字，可以不调用
     *
     * @param titles 标题
     */
    public void setTitleData(ArrayList<String> titles) {
        this.titleData.clear();
        this.titleData.addAll(titles);
        if (titleData.size() > 0) {
            title.setText(titleData.get(mViewPager.getCurrentItem() % mAdapter.getDataCount()));//设置默认标题
        }
    }

    /**
     * 设置视图数据
     * 一般情况下作为轮播图使用，请传入存有ImageView的ArrayList，图像加载操作本控件不负责
     *
     * @param data 视图资源，不仅限于ImageView。
     */
    public void setViewData(ArrayList data) {
        this.mAdapter = new ADBarAdapter<>(data);
        if (mAdapter.getDataCount() != 0) {
            AutoRoll();
            mViewPager.setAdapter(mAdapter);
            mViewPager.setCurrentItem(Integer.MAX_VALUE / 2 - (Integer.MAX_VALUE / 2 % mAdapter.getDataCount()));

            initPoint();
        }


        /**
         * 设置滑动监听
         */
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()

                                           {
                                               @Override
                                               public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                                               }

                                               @Override
                                               public void onPageSelected(int position) {
                                                   if (titleData.size() > 0) {
                                                       title.setText(titleData.get(position % mAdapter.getDataCount()));
                                                   }
                                                   for (ImageView im : points
                                                           ) {
                                                       im.setImageBitmap(normalPoint);
                                                   }
                                                   points.get(position % mAdapter.getDataCount()).setImageBitmap(selectPoint);
                                               }

                                               @Override
                                               public void onPageScrollStateChanged(int state) {

                                               }
                                           }

        );

    }

    /**
     * 初始化圆点
     */
    private void initPoint() {
        LinearLayout linearLayout = new LinearLayout(mContext);//圆点是父控件
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);//设置为水平方向
        LayoutParams linearLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayoutParams.addRule(ALIGN_PARENT_BOTTOM);
        linearLayoutParams.addRule(ALIGN_PARENT_RIGHT);
        linearLayoutParams.setMargins(0, 0, 5, 5);
        linearLayout.setLayoutParams(linearLayoutParams);
        for (int i = 0; i < mAdapter.getDataCount(); i++) {
            ImageView imageView = new ImageView(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20, 20);
            params.setMargins(5, 5, 5, 5);
            imageView.setLayoutParams(params);
            if (i == 0) {
                imageView.setImageBitmap(selectPoint);
            } else {
                imageView.setImageBitmap(normalPoint);
            }
            linearLayout.addView(imageView);
            points.add(imageView);
        }
        addView(linearLayout);
    }

    private void AutoRoll() {
        if (isAutoRoll){
            mHandler.sendEmptyMessageDelayed(0, delayTime);
        }

    }

    /**
     * 设置是否滚动，请在setViewData之前调用，默认滚动
     * @param b 是否滚动参数
     */
    public void setAutoRoll(boolean b){
        this.isAutoRoll=b;
    }

    /**
     * 通知适配器数据发生变化
     */
    public void notifyDataChanged() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 设置页面点击事件
     *
     * @param listener onPageClickListener
     */
    public void setOnPageClickListener(onPageClickListener listener) {

        this.mListener = listener;
    }

    //点击页面监听接口
    public interface onPageClickListener {
        void onPageClick(Object object, int position);

    }

    /**
     * 取得小圆点bitmap
     *
     * @param color 相应颜色
     * @return 相应颜色的Bitmap
     */
    public Bitmap getBitmap(int color) {
        Bitmap bm = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawCircle(10, 10, 10, paint);
        return bm;
    }


}
