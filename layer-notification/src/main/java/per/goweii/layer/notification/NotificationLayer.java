package per.goweii.layer.notification;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import per.goweii.layer.core.DecorLayer;
import per.goweii.layer.core.anim.AnimatorHelper;
import per.goweii.layer.core.utils.Utils;
import per.goweii.layer.core.widget.MaxSizeFrameLayout;
import per.goweii.layer.core.widget.SwipeLayout;

public class NotificationLayer extends DecorLayer {

    private Runnable mDismissRunnable = null;
    private boolean mSwiping = false;

    public NotificationLayer(@NonNull Context context) {
        this(Utils.requireActivity(context));
    }

    public NotificationLayer(@NonNull Activity activity) {
        super(activity);
    }

    @IntRange(from = 0)
    @Override
    protected int getLevel() {
        return Level.NOTIFICATION;
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
        SwipeLayout container = new SwipeLayout(context);
        container.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        MaxSizeFrameLayout contentWrapper = new MaxSizeFrameLayout(context);
        contentWrapper.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        getViewHolder().setContentWrapper(contentWrapper);
        container.addView(contentWrapper);
        if (getViewHolder().getContentNullable() == null) {
            getViewHolder().setContent(onCreateContent(inflater, contentWrapper));
        }
        View content = getViewHolder().getContent();
        contentWrapper.addView(content);
        return container;
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
        if (content.getLayoutParams() == null) {
            content.setLayoutParams(generateContentDefaultLayoutParams());
        }
        return content;
    }

    @NonNull
    protected FrameLayout.LayoutParams generateContentDefaultLayoutParams() {
        return new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
    }

    @Nullable
    @Override
    protected Animator onCreateInAnimator(@NonNull View view) {
        return AnimatorHelper.createTopInAnim(getViewHolder().getContentWrapper());
    }

    @Nullable
    @Override
    protected Animator onCreateOutAnimator(@NonNull View view) {
        return AnimatorHelper.createTopOutAnim(getViewHolder().getContentWrapper());
    }

    @CallSuper
    @Override
    protected void onAttach() {
        super.onAttach();
        getViewHolder().getChild().setSwipeDirection(
                SwipeLayout.Direction.TOP | SwipeLayout.Direction.LEFT | SwipeLayout.Direction.RIGHT
        );
        getViewHolder().getChild().setOnSwipeListener(new SwipeLayout.OnSwipeListener() {
            @Override
            public void onStart(@SwipeLayout.Direction int direction, @FloatRange(from = 0F, to = 1F) float fraction) {
                mSwiping = true;
                setAutoDismiss(false);
                getListenerHolder().notifyOnSwipeStart(NotificationLayer.this);
            }

            @Override
            public void onSwiping(@SwipeLayout.Direction int direction, @FloatRange(from = 0F, to = 1F) float fraction) {
                getListenerHolder().notifyOnSwiping(NotificationLayer.this, direction, fraction);
            }

            @Override
            public void onEnd(@SwipeLayout.Direction int direction, @FloatRange(from = 0F, to = 1F) float fraction) {
                mSwiping = false;
                if (fraction == 1F) {
                    getListenerHolder().notifyOnSwipeEnd(NotificationLayer.this, direction);
                    // 动画执行结束后不能直接removeView，要在下一个dispatchDraw周期移除
                    // 否则会崩溃，因为viewGroup的childCount没有来得及-1，获取到的view为空
                    getViewHolder().getContent().setVisibility(View.INVISIBLE);
                    getViewHolder().getContent().post(new Runnable() {
                        @Override
                        public void run() {
                            dismiss(false);
                        }
                    });
                } else if (fraction == 0F) {
                    setAutoDismiss(true);
                }
            }
        });
        if (getConfig().mMaxWidth >= 0) {
            getViewHolder().getContentWrapper().setMaxWidth(getConfig().mMaxWidth);
        }
        if (getConfig().mMaxHeight >= 0) {
            getViewHolder().getContentWrapper().setMaxHeight(getConfig().mMaxHeight);
        }
        getViewHolder().getContent().setVisibility(View.VISIBLE);
        getViewHolder().getContentWrapper().setOnDispatchTouchListener(new MaxSizeFrameLayout.OnDispatchTouchListener() {
            @Override
            public void onDispatch(MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setAutoDismiss(false);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        if (!mSwiping) {
                            setAutoDismiss(true);
                        }
                        break;
                }
            }
        });
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
        setAutoDismiss(true);
    }

    @CallSuper
    @Override
    protected void onPreDismiss() {
        if (mDismissRunnable != null) {
            getChild().removeCallbacks(mDismissRunnable);
        }
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
    }

    @CallSuper
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void fitDecorInsides() {
        fitDecorInsidesToViewPadding(getViewHolder().getChild());
        getViewHolder().getChild().setClipToPadding(false);
    }

    @NonNull
    public NotificationLayer setContentView(@NonNull View contentView) {
        getConfig().mContentView = contentView;
        return this;
    }

    @NonNull
    public NotificationLayer setContentView(@LayoutRes int contentViewId) {
        getConfig().mContentViewId = contentViewId;
        return this;
    }

    @NonNull
    public NotificationLayer setMaxWidth(int maxWidth) {
        getConfig().mMaxWidth = maxWidth;
        return this;
    }

    @NonNull
    public NotificationLayer setMaxHeight(int maxHeight) {
        getConfig().mMaxHeight = maxHeight;
        return this;
    }

    @NonNull
    public NotificationLayer setOnNotificationClickListener(@NonNull OnClickListener listener) {
        addOnClickToDismissListener(listener);
        return this;
    }

    @NonNull
    public NotificationLayer setOnNotificationLongClickListener(@NonNull OnLongClickListener listener) {
        addOnLongClickToDismissListener(listener);
        return this;
    }

    public void setAutoDismiss(boolean enable) {
        if (mDismissRunnable != null) {
            getChild().removeCallbacks(mDismissRunnable);
        }
        if (enable && isShown() && getConfig().mDuration > 0) {
            if (mDismissRunnable == null) {
                mDismissRunnable = new Runnable() {
                    @Override
                    public void run() {
                        dismiss();
                    }
                };
            }
            getChild().postDelayed(mDismissRunnable, getConfig().mDuration);
        }
    }

    @NonNull
    public NotificationLayer setDuration(long duration) {
        getConfig().mDuration = duration;
        return this;
    }

    /**
     * 浮层拖拽事件监听
     *
     * @param swipeListener OnSwipeListener
     */
    @NonNull
    public NotificationLayer addOnSwipeListener(@NonNull NotificationLayer.OnSwipeListener swipeListener) {
        getListenerHolder().addOnSwipeListener(swipeListener);
        return this;
    }

    public static class ViewHolder extends DecorLayer.ViewHolder {
        private MaxSizeFrameLayout mContentWrapper;
        private View mContent;

        @NonNull
        @Override
        public SwipeLayout getChild() {
            return (SwipeLayout) super.getChild();
        }

        @Nullable
        @Override
        protected SwipeLayout getChildNullable() {
            return (SwipeLayout) super.getChildNullable();
        }

        public void setContentWrapper(@NonNull MaxSizeFrameLayout contentWrapper) {
            mContentWrapper = contentWrapper;
        }

        @NonNull
        protected MaxSizeFrameLayout getContentWrapper() {
            return mContentWrapper;
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

        @Nullable
        @Override
        protected View getNoIdClickView() {
            return mContent;
        }
    }

    protected static class Config extends DecorLayer.Config {
        protected View mContentView = null;
        protected int mContentViewId = -1;

        protected long mDuration = 5000L;
        protected int mMaxWidth = -1;
        protected int mMaxHeight = -1;
    }

    protected static class ListenerHolder extends DecorLayer.ListenerHolder {
        private List<OnSwipeListener> mOnSwipeListeners = null;

        private void addOnSwipeListener(@NonNull OnSwipeListener onSwipeListener) {
            if (mOnSwipeListeners == null) {
                mOnSwipeListeners = new ArrayList<>(1);
            }
            mOnSwipeListeners.add(onSwipeListener);
        }

        private void notifyOnSwipeStart(@NonNull NotificationLayer layer) {
            if (mOnSwipeListeners != null) {
                for (OnSwipeListener onSwipeListener : mOnSwipeListeners) {
                    onSwipeListener.onStart(layer);
                }
            }
        }

        private void notifyOnSwiping(@NonNull NotificationLayer layer,
                                     @SwipeLayout.Direction int direction,
                                     @FloatRange(from = 0F, to = 1F) float fraction) {
            if (mOnSwipeListeners != null) {
                for (OnSwipeListener onSwipeListener : mOnSwipeListeners) {
                    onSwipeListener.onSwiping(layer, direction, fraction);
                }
            }
        }

        private void notifyOnSwipeEnd(@NonNull NotificationLayer layer,
                                      @SwipeLayout.Direction int direction) {
            if (mOnSwipeListeners != null) {
                for (OnSwipeListener onSwipeListener : mOnSwipeListeners) {
                    onSwipeListener.onEnd(layer, direction);
                }
            }
        }
    }

    public interface OnSwipeListener {
        void onStart(@NonNull NotificationLayer layer);

        void onSwiping(@NonNull NotificationLayer layer,
                       @SwipeLayout.Direction int direction,
                       @FloatRange(from = 0F, to = 1F) float fraction);

        void onEnd(@NonNull NotificationLayer layer,
                   @SwipeLayout.Direction int direction);
    }
}