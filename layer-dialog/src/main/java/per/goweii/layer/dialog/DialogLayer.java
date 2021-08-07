package per.goweii.layer.dialog;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

import java.util.ArrayList;
import java.util.List;

import per.goweii.layer.core.DecorLayer;
import per.goweii.layer.core.Layers;
import per.goweii.layer.core.anim.AnimatorHelper;
import per.goweii.layer.core.utils.SoftInputCompat;
import per.goweii.layer.core.utils.Utils;
import per.goweii.layer.core.widget.SwipeLayout;

public class DialogLayer extends DecorLayer {
    private static final long ANIM_DUR_DEF = 220L;
    private static final float DIM_AMOUNT_DEF = 0.6F;

    private SoftInputCompat mSoftInputCompat = null;

    public static void create(@NonNull DialogLayerActivity.OnLayerCreatedCallback callback) {
        DialogLayerActivity.start(Layers.getApplication(), callback);
    }

    public DialogLayer() {
        this(Layers.requireCurrentActivity());
    }

    public DialogLayer(@NonNull Context context) {
        this(Utils.requireActivity(context));
    }

    public DialogLayer(@NonNull Activity activity) {
        super(activity);
        setCancelableOnKeyBack(true);
    }

    @IntRange(from = 0)
    @Override
    protected int getLevel() {
        return Level.DIALOG;
    }

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder() {
        return new ViewHolder();
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder() {
        return (ViewHolder) super.getViewHolder();
    }

    @NonNull
    @Override
    protected Config onCreateConfig() {
        return new Config();
    }

    @NonNull
    @Override
    public Config getConfig() {
        return (Config) super.getConfig();
    }

    @NonNull
    @Override
    protected ListenerHolder onCreateListenerHolder() {
        return new ListenerHolder();
    }

    @NonNull
    @Override
    public ListenerHolder getListenerHolder() {
        return (ListenerHolder) super.getListenerHolder();
    }

    @NonNull
    @Override
    protected View onCreateChild(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        Context context = getActivity();
        ContainerLayout container = new ContainerLayout(context);
        if (getViewHolder().getBackgroundNullable() == null) {
            getViewHolder().setBackground(onCreateBackground(inflater, container));
        }
        View background = getViewHolder().getBackground();
        Utils.removeViewParent(background);
        container.addView(background, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        SwipeLayout contentWrapper = new SwipeLayout(context);
        getViewHolder().setContentWrapper(contentWrapper);
        if (getViewHolder().getContentNullable() == null) {
            getViewHolder().setContent(onCreateContent(inflater, contentWrapper));
        }
        View content = getViewHolder().getContent();
        Utils.removeViewParent(content);
        contentWrapper.addView(content);
        container.addView(contentWrapper, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        return container;
    }

    @NonNull
    protected View onCreateBackground(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View background;
        if (getConfig().mBackgroundView != null) {
            background = getConfig().mBackgroundView;
        } else if (getConfig().mBackgroundViewId > 0) {
            background = inflater.inflate(getConfig().mBackgroundViewId, parent, false);
        } else {
            View view = new View(getActivity());
            view.setBackgroundColor(getConfig().mBackgroundColor);
            background = view;
        }
        return background;
    }

    @NonNull
    protected View onCreateContent(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View content;
        if (getConfig().mContentView != null) {
            content = getConfig().mContentView;
        } else {
            content = inflater.inflate(getConfig().mContentViewId, parent, false);
        }
        Utils.removeViewParent(content);
        ViewGroup.LayoutParams layoutParams = content.getLayoutParams();
        FrameLayout.LayoutParams contentParams;
        if (layoutParams == null) {
            contentParams = generateContentDefaultLayoutParams();
        } else if (layoutParams instanceof FrameLayout.LayoutParams) {
            contentParams = (FrameLayout.LayoutParams) layoutParams;
        } else {
            contentParams = new FrameLayout.LayoutParams(layoutParams.width, layoutParams.height);
        }
        if (getConfig().mGravity != -1) {
            contentParams.gravity = getConfig().mGravity;
        }
        content.setLayoutParams(contentParams);
        return content;
    }

    @Nullable
    @Override
    protected Animator onCreateInAnimator(@NonNull View view) {
        Animator backgroundAnimator = onCreateBackgroundInAnimator(getViewHolder().getBackground());
        Animator contentAnimator = onCreateContentInAnimator(getViewHolder().getContent());
        if (backgroundAnimator == null && contentAnimator == null) return null;
        if (backgroundAnimator == null) return contentAnimator;
        if (contentAnimator == null) return backgroundAnimator;
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(backgroundAnimator, contentAnimator);
        return animatorSet;
    }

    @Nullable
    protected Animator onCreateBackgroundInAnimator(@NonNull View view) {
        Animator backgroundAnimator;
        if (getConfig().mBackgroundAnimatorCreator != null) {
            backgroundAnimator = getConfig().mBackgroundAnimatorCreator.createInAnimator(view);
        } else {
            backgroundAnimator = onCreateDefBackgroundInAnimator(view);
        }
        return backgroundAnimator;
    }

    @NonNull
    protected Animator onCreateDefBackgroundInAnimator(@NonNull View view) {
        Animator animator = AnimatorHelper.createAlphaInAnim(view);
        animator.setDuration(ANIM_DUR_DEF);
        return animator;
    }

    @Nullable
    protected Animator onCreateContentInAnimator(@NonNull View view) {
        Animator contentAnimator;
        if (getConfig().mContentAnimatorCreator != null) {
            contentAnimator = getConfig().mContentAnimatorCreator.createInAnimator(view);
        } else {
            contentAnimator = onCreateDefContentInAnimator(view);
        }
        return contentAnimator;
    }

    @NonNull
    protected Animator onCreateDefContentInAnimator(@NonNull View view) {
        Animator animator = AnimatorHelper.createZoomAlphaInAnim(view);
        animator.setDuration(ANIM_DUR_DEF);
        return animator;
    }

    @Nullable
    @Override
    protected Animator onCreateOutAnimator(@NonNull View view) {
        Animator backgroundAnimator = onCreateBackgroundOutAnimator(getViewHolder().getBackground());
        Animator contentAnimator = onCreateContentOutAnimator(getViewHolder().getContent());
        if (backgroundAnimator == null && contentAnimator == null) return null;
        if (backgroundAnimator == null) return contentAnimator;
        if (contentAnimator == null) return backgroundAnimator;
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(backgroundAnimator, contentAnimator);
        return animatorSet;
    }

    @Nullable
    protected Animator onCreateBackgroundOutAnimator(@NonNull View view) {
        Animator backgroundAnimator;
        if (getConfig().mBackgroundAnimatorCreator != null) {
            backgroundAnimator = getConfig().mBackgroundAnimatorCreator.createOutAnimator(view);
        } else {
            backgroundAnimator = onCreateDefBackgroundOutAnimator(view);
        }
        return backgroundAnimator;
    }

    @NonNull
    protected Animator onCreateDefBackgroundOutAnimator(@NonNull View view) {
        Animator animator = AnimatorHelper.createAlphaOutAnim(view);
        animator.setDuration(ANIM_DUR_DEF);
        return animator;
    }

    @Nullable
    protected Animator onCreateContentOutAnimator(@NonNull View view) {
        Animator contentAnimator;
        if (getConfig().mContentAnimatorCreator != null) {
            contentAnimator = getConfig().mContentAnimatorCreator.createOutAnimator(view);
        } else {
            contentAnimator = onCreateDefContentOutAnimator(view);
        }
        return contentAnimator;
    }

    @NonNull
    protected Animator onCreateDefContentOutAnimator(@NonNull View view) {
        Animator animator = AnimatorHelper.createZoomAlphaOutAnim(view);
        animator.setDuration(ANIM_DUR_DEF);
        return animator;
    }

    @NonNull
    protected FrameLayout.LayoutParams generateContentDefaultLayoutParams() {
        return new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
    }

    @CallSuper
    @Override
    protected void onCreate() {
        super.onCreate();
    }

    @CallSuper
    @Override
    protected void onAttach() {
        onInitContent();
        onInitBackground();
        onInitContainer();
        super.onAttach();
        registerSoftInputCompat();
    }

    @CallSuper
    @Override
    protected void onPreShow() {
        super.onPreShow();
    }

    @CallSuper
    @Override
    protected void onPostShow() {
        super.onPostShow();
    }

    @CallSuper
    @Override
    protected void onPreDismiss() {
        super.onPreDismiss();
    }

    @CallSuper
    @Override
    protected void onPostDismiss() {
        super.onPostDismiss();
    }

    @CallSuper
    @Override
    protected void onDetach() {
        super.onDetach();
        unregisterSoftInputCompat();
    }

    @CallSuper
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void fitDecorInsides() {
        fitDecorInsidesToViewPadding(getViewHolder().getContentWrapper());
        if (getConfig().mAvoidStatusBar) {
            int paddingTop = getViewHolder().getContentWrapper().getPaddingTop();
            int statusBarHeight = Utils.getStatusBarHeight(getActivity());
            Utils.setViewPaddingTop(getViewHolder().getContentWrapper(), Math.max(paddingTop, statusBarHeight));
        }
        getViewHolder().getContentWrapper().setClipToPadding(false);
        getViewHolder().getContentWrapper().setClipChildren(false);
    }

    protected void onInitContent() {
        getViewHolder().getContent().setClickable(true);
    }

    protected void onInitBackground() {
    }

    protected void onInitContainer() {
        if (getConfig().mOutsideInterceptTouchEvent) {
            getViewHolder().getContainer().setFocusInside(true);
            getViewHolder().getContainer().setHandleTouchEvent(true);
            if (getConfig().mCancelableOnTouchOutside) {
                getViewHolder().getContainer().setOnTappedListener(new ContainerLayout.OnTappedListener() {
                    @Override
                    public void onTapped() {
                        dismiss();
                    }
                });
            }
        } else {
            getViewHolder().getContainer().setOnTappedListener(null);
            getViewHolder().getContainer().setFocusInside(false);
            getViewHolder().getContainer().setHandleTouchEvent(false);
        }
        if (getConfig().mOutsideTouchedToDismiss || getConfig().mOnOutsideTouchListener != null) {
            getViewHolder().getContainer().setOnTouchedListener(new ContainerLayout.OnTouchedListener() {
                @Override
                public void onTouched() {
                    if (getConfig().mOutsideTouchedToDismiss) {
                        dismiss();
                    }
                    if (getConfig().mOnOutsideTouchListener != null) {
                        getConfig().mOnOutsideTouchListener.outsideTouched();
                    }
                }
            });
        }
        FrameLayout.LayoutParams contentWrapperParams = (FrameLayout.LayoutParams) getViewHolder().getContentWrapper().getLayoutParams();
        contentWrapperParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
        contentWrapperParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
        getViewHolder().getContentWrapper().setLayoutParams(contentWrapperParams);
        getViewHolder().getContentWrapper().setSwipeDirection(getConfig().mSwipeDirection);
        getViewHolder().getContentWrapper().setOnSwipeListener(new SwipeLayout.OnSwipeListener() {
            @Override
            public void onStart(@SwipeLayout.Direction int direction, @FloatRange(from = 0F, to = 1F) float fraction) {
                if (getConfig().mSwipeTransformer == null) {
                    getConfig().mSwipeTransformer = new SwipeTransformer() {
                        @Override
                        public void onSwiping(@NonNull DialogLayer layer, @SwipeLayout.Direction int direction, @FloatRange(from = 0F, to = 1F) float fraction) {
                            layer.getViewHolder().getBackground().setAlpha(1F - fraction);
                        }
                    };
                }
                getListenerHolder().notifyOnSwipeStart(DialogLayer.this);
            }

            @Override
            public void onSwiping(@SwipeLayout.Direction int direction, @FloatRange(from = 0F, to = 1F) float fraction) {
                if (getConfig().mSwipeTransformer != null) {
                    getConfig().mSwipeTransformer.onSwiping(DialogLayer.this, direction, fraction);
                }
                getListenerHolder().notifyOnSwiping(DialogLayer.this, direction, fraction);
            }

            @Override
            public void onEnd(@SwipeLayout.Direction int direction, @FloatRange(from = 0F, to = 1F) float fraction) {
                if (fraction == 1F) {
                    getListenerHolder().notifyOnSwipeEnd(DialogLayer.this, direction);
                    // 动画执行结束后不能直接removeView，要在下一个dispatchDraw周期移除
                    // 否则会崩溃，因为viewGroup的childCount没有来得及-1，获取到的view为空
                    getViewHolder().getContentWrapper().setVisibility(View.INVISIBLE);
                    getViewHolder().getContentWrapper().post(new Runnable() {
                        @Override
                        public void run() {
                            dismiss(false);
                        }
                    });
                }
            }
        });
        getViewHolder().getContentWrapper().setVisibility(View.VISIBLE);
    }

    private void registerSoftInputCompat() {
        final SparseBooleanArray mapping = getConfig().mSoftInputMapping;
        if (mapping == null || mapping.size() == 0) {
            return;
        }
        if (mSoftInputCompat == null) {
            mSoftInputCompat = SoftInputCompat.attach(getActivity());
        } else {
            mSoftInputCompat.clear();
        }
        mSoftInputCompat.setListener(new SoftInputCompat.OnSoftInputListener() {
            @Override
            public void onOpen(int height) {
                getListenerHolder().notifyOnSoftInputOpen(DialogLayer.this, height);
            }

            @Override
            public void onClose(int height) {
                getListenerHolder().notifyOnSoftInputClose(DialogLayer.this, height);
            }

            @Override
            public void onHeightChange(int height) {
                getListenerHolder().notifyOnSoftInputHeightChange(DialogLayer.this, height);
            }
        });
        mSoftInputCompat.setMoveView(getViewHolder().getContentWrapper());
        for (int i = 0; i < mapping.size(); i++) {
            boolean alignToContentOrFocus = mapping.valueAt(i);
            int focusId = mapping.keyAt(i);
            if (focusId == View.NO_ID) {
                if (alignToContentOrFocus) {
                    mSoftInputCompat.setFollowViews(getViewHolder().getContent());
                }
            } else {
                if (alignToContentOrFocus) {
                    mSoftInputCompat.setFollowViews(getViewHolder().getContent(), findView(focusId));
                } else {
                    mSoftInputCompat.setFollowViews(null, findView(focusId));
                }
            }
        }
    }

    private void unregisterSoftInputCompat() {
        if (mSoftInputCompat != null) {
            mSoftInputCompat.setListener(null);
            mSoftInputCompat.clear();
            mSoftInputCompat.detach();
            mSoftInputCompat = null;
        }
    }

    /**
     * 设置自自定义View
     *
     * @param contentView 自定以View
     */
    @NonNull
    public DialogLayer setContentView(@Nullable View contentView) {
        getConfig().mContentView = contentView;
        return this;
    }

    /**
     * 设置自定义布局文件
     *
     * @param contentViewId 自定义布局ID
     */
    @NonNull
    public DialogLayer setContentView(@LayoutRes int contentViewId) {
        getConfig().mContentViewId = contentViewId;
        return this;
    }

    /**
     * 设置自自定义背景View
     *
     * @param backgroundView 自定以背景View
     */
    @NonNull
    public DialogLayer setBackgroundView(@Nullable View backgroundView) {
        getConfig().mBackgroundView = backgroundView;
        return this;
    }

    /**
     * 设置背景布局文件
     *
     * @param backgroundViewId 自定义布局ID
     */
    @NonNull
    public DialogLayer setBackgroundView(@LayoutRes int backgroundViewId) {
        getConfig().mBackgroundViewId = backgroundViewId;
        return this;
    }

    /**
     * 设置避开状态栏
     *
     * @param avoid 设置避开状态栏
     */
    @NonNull
    public DialogLayer setAvoidStatusBar(boolean avoid) {
        getConfig().mAvoidStatusBar = avoid;
        return this;
    }

    /**
     * 设置子布局的gravity
     * 可直接在布局文件指定layout_gravity属性，作用相同
     *
     * @param gravity {@link Gravity}
     */
    @NonNull
    public DialogLayer setGravity(int gravity) {
        getConfig().mGravity = gravity;
        return this;
    }

    /**
     * 自定义浮层的拖拽退出的方向
     *
     * @param swipeDirection {@link SwipeLayout.Direction}
     */
    @NonNull
    public DialogLayer setSwipeDismiss(int swipeDirection) {
        getConfig().mSwipeDirection = swipeDirection;
        return this;
    }

    /**
     * 自定义浮层的拖拽退出时的动画
     *
     * @param swipeTransformer SwipeTransformer
     */
    @NonNull
    public DialogLayer setSwipeTransformer(@Nullable SwipeTransformer swipeTransformer) {
        getConfig().mSwipeTransformer = swipeTransformer;
        return this;
    }

    /**
     * 浮层拖拽事件监听
     *
     * @param swipeListener OnSwipeListener
     */
    @NonNull
    public DialogLayer addOnSwipeListener(@NonNull OnSwipeListener swipeListener) {
        getListenerHolder().addOnSwipeListener(swipeListener);
        return this;
    }

    /**
     * 自定义浮层的进入和退出动画
     * 可使用工具类{@link AnimatorHelper}
     *
     * @param contentAnimatorCreator AnimatorCreator
     */
    @NonNull
    public DialogLayer setContentAnimator(@Nullable AnimatorCreator contentAnimatorCreator) {
        getConfig().mContentAnimatorCreator = contentAnimatorCreator;
        return this;
    }

    /**
     * 自定义背景的进入和退出动画
     * 可使用工具类{@link AnimatorHelper}
     *
     * @param backgroundAnimatorCreator AnimatorCreator
     */
    @NonNull
    public DialogLayer setBackgroundAnimator(@Nullable AnimatorCreator backgroundAnimatorCreator) {
        getConfig().mBackgroundAnimatorCreator = backgroundAnimatorCreator;
        return this;
    }

    /**
     * 设置背景变暗程度
     *
     * @param dimAmount 变暗程度 0~1
     */
    @NonNull
    public DialogLayer setBackgroundDimAmount(@FloatRange(from = 0F, to = 1F) float dimAmount) {
        getConfig().mBackgroundColor = Color.argb((int) (dimAmount * 255), 0, 0, 0);
        return this;
    }

    /**
     * 设置背景变暗
     */
    @NonNull
    public DialogLayer setBackgroundDimDefault() {
        return setBackgroundDimAmount(DIM_AMOUNT_DEF);
    }


    /**
     * 设置背景颜色
     *
     * @param colorInt 颜色值
     */
    @NonNull
    public DialogLayer setBackgroundColorInt(@ColorInt int colorInt) {
        getConfig().mBackgroundColor = colorInt;
        return this;
    }

    /**
     * 设置背景颜色
     *
     * @param colorRes 颜色资源ID
     */
    @NonNull
    public DialogLayer setBackgroundColorRes(@ColorRes int colorRes) {
        getConfig().mBackgroundColor = getActivity().getResources().getColor(colorRes);
        return this;
    }

    /**
     * 设置点击浮层以外区域是否可关闭
     *
     * @param cancelable 是否可关闭
     */
    @NonNull
    public DialogLayer setCancelableOnTouchOutside(boolean cancelable) {
        getConfig().mCancelableOnTouchOutside = cancelable;
        return this;
    }

    /**
     * 设置点击返回键是否可关闭
     *
     * @param cancelable 是否可关闭
     */
    @NonNull
    @Override
    public DialogLayer setCancelableOnClickKeyBack(boolean cancelable) {
        return (DialogLayer) super.setCancelableOnClickKeyBack(cancelable);
    }

    /**
     * 适配软键盘的弹出，布局自动上移
     * 在某几个View获取焦点时布局上移
     *
     * @param alignToContentOrFocus true为对齐到contentView，false为对齐到focusView自身
     * @param focusIds              焦点View
     */
    @NonNull
    public DialogLayer addSoftInputCompat(boolean alignToContentOrFocus, @Nullable int... focusIds) {
        if (getConfig().mSoftInputMapping == null) {
            getConfig().mSoftInputMapping = new SparseBooleanArray(1);
        }
        if (focusIds != null && focusIds.length > 0) {
            for (int focusId : focusIds) {
                getConfig().mSoftInputMapping.append(focusId, alignToContentOrFocus);
            }
        } else {
            getConfig().mSoftInputMapping.append(View.NO_ID, alignToContentOrFocus);
        }
        return this;
    }

    public DialogLayer addOnSoftInputListener(@NonNull OnSoftInputListener onSoftInputListener) {
        getListenerHolder().addOnSoftInputListener(onSoftInputListener);
        return this;
    }

    /**
     * 设置浮层外部是否拦截触摸
     * 默认为true，false则事件有activityContent本身消费
     *
     * @param intercept 外部是否拦截触摸
     */
    @NonNull
    public DialogLayer setOutsideInterceptTouchEvent(boolean intercept) {
        getConfig().mOutsideInterceptTouchEvent = intercept;
        return this;
    }

    @NonNull
    public DialogLayer setOnOutsideTouchListener(OnOutsideTouchListener listener) {
        getConfig().mOnOutsideTouchListener = listener;
        return this;
    }

    @NonNull
    public DialogLayer setOutsideTouchToDismiss(boolean toDismiss) {
        getConfig().mOutsideTouchedToDismiss = toDismiss;
        return this;
    }

    public static class ViewHolder extends DecorLayer.ViewHolder {
        private View mBackground;
        private SwipeLayout mContentWrapper;
        private View mContent;

        public void setContentWrapper(@NonNull SwipeLayout contentWrapper) {
            mContentWrapper = contentWrapper;
        }

        public void setBackground(@NonNull View background) {
            mBackground = background;
        }

        @NonNull
        public ContainerLayout getContainer() {
            return getChild();
        }

        @NonNull
        @Override
        public ContainerLayout getChild() {
            return (ContainerLayout) super.getChild();
        }

        @Nullable
        @Override
        protected ContainerLayout getChildNullable() {
            return (ContainerLayout) super.getChildNullable();
        }

        protected void setContent(@NonNull View content) {
            mContent = content;
        }

        @Nullable
        protected View getContentNullable() {
            return mContent;
        }

        @NonNull
        public View getContent() {
            Utils.requireNonNull(mContent, "必须在show方法后调用");
            return mContent;
        }

        @NonNull
        public SwipeLayout getContentWrapper() {
            return mContentWrapper;
        }

        @Nullable
        protected View getBackgroundNullable() {
            return mBackground;
        }

        @NonNull
        public View getBackground() {
            return mBackground;
        }
    }

    protected static class Config extends DecorLayer.Config {
        protected boolean mOutsideInterceptTouchEvent = true;
        @Nullable
        protected OnOutsideTouchListener mOnOutsideTouchListener = null;
        protected boolean mOutsideTouchedToDismiss = false;

        @Nullable
        protected AnimatorCreator mBackgroundAnimatorCreator = null;
        @Nullable
        protected AnimatorCreator mContentAnimatorCreator = null;

        protected View mContentView = null;
        protected int mContentViewId = -1;

        protected View mBackgroundView = null;
        protected int mBackgroundViewId = -1;
        @ColorInt
        protected int mBackgroundColor = Color.TRANSPARENT;

        protected boolean mCancelableOnTouchOutside = true;

        protected boolean mAvoidStatusBar = false;

        protected int mGravity = Gravity.CENTER;
        @SwipeLayout.Direction
        protected int mSwipeDirection = 0;
        @Nullable
        protected SwipeTransformer mSwipeTransformer = null;

        protected SparseBooleanArray mSoftInputMapping = null;
    }

    protected static class ListenerHolder extends DecorLayer.ListenerHolder {
        private List<OnSwipeListener> mOnSwipeListeners = null;
        private List<OnSoftInputListener> mOnSoftInputListeners = null;

        private void addOnSwipeListener(@NonNull OnSwipeListener onSwipeListener) {
            if (mOnSwipeListeners == null) {
                mOnSwipeListeners = new ArrayList<>(1);
            }
            mOnSwipeListeners.add(onSwipeListener);
        }

        private void addOnSoftInputListener(@NonNull OnSoftInputListener onSoftInputListener) {
            if (mOnSoftInputListeners == null) {
                mOnSoftInputListeners = new ArrayList<>(1);
            }
            mOnSoftInputListeners.add(onSoftInputListener);
        }

        private void notifyOnSwipeStart(@NonNull DialogLayer layer) {
            if (mOnSwipeListeners != null) {
                for (OnSwipeListener onSwipeListener : mOnSwipeListeners) {
                    onSwipeListener.onStart(layer);
                }
            }
        }

        private void notifyOnSwiping(@NonNull DialogLayer layer,
                                     @SwipeLayout.Direction int direction,
                                     @FloatRange(from = 0F, to = 1F) float fraction) {
            if (mOnSwipeListeners != null) {
                for (OnSwipeListener onSwipeListener : mOnSwipeListeners) {
                    onSwipeListener.onSwiping(layer, direction, fraction);
                }
            }
        }

        private void notifyOnSwipeEnd(@NonNull DialogLayer layer,
                                      @SwipeLayout.Direction int direction) {
            if (mOnSwipeListeners != null) {
                for (OnSwipeListener onSwipeListener : mOnSwipeListeners) {
                    onSwipeListener.onEnd(layer, direction);
                }
            }
        }

        private void notifyOnSoftInputOpen(@NonNull DialogLayer layer, @Px int height) {
            if (mOnSoftInputListeners != null) {
                for (OnSoftInputListener onSoftInputListener : mOnSoftInputListeners) {
                    onSoftInputListener.onOpen(layer, height);
                }
            }
        }

        private void notifyOnSoftInputClose(@NonNull DialogLayer layer, @Px int height) {
            if (mOnSoftInputListeners != null) {
                for (OnSoftInputListener onSoftInputListener : mOnSoftInputListeners) {
                    onSoftInputListener.onClose(layer, height);
                }
            }
        }

        private void notifyOnSoftInputHeightChange(@NonNull DialogLayer layer, @Px int height) {
            if (mOnSoftInputListeners != null) {
                for (OnSoftInputListener onSoftInputListener : mOnSoftInputListeners) {
                    onSoftInputListener.onHeightChange(layer, height);
                }
            }
        }
    }

    public interface OnOutsideTouchListener {
        void outsideTouched();
    }

    public interface SwipeTransformer {
        void onSwiping(@NonNull DialogLayer layer,
                       @SwipeLayout.Direction int direction,
                       @FloatRange(from = 0F, to = 1F) float fraction);
    }

    public interface OnSwipeListener {
        void onStart(@NonNull DialogLayer layer);

        void onSwiping(@NonNull DialogLayer layer,
                       @SwipeLayout.Direction int direction,
                       @FloatRange(from = 0F, to = 1F) float fraction);

        void onEnd(@NonNull DialogLayer layer,
                   @SwipeLayout.Direction int direction);
    }

    public interface OnSoftInputListener {
        void onOpen(@NonNull DialogLayer layer, @Px int height);

        void onClose(@NonNull DialogLayer layer, @Px int height);

        void onHeightChange(@NonNull DialogLayer layer, @Px int height);
    }
}
