package eu.inloop.viewmodel;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.UUID;

public class ViewModelHelper<T extends IView, R extends AbstractViewModel<T>> {

    private String mScreenId;
    private R mViewModel;
    private boolean mModelRemoved;

    /**
     * Call from {@link android.app.Activity#onCreate(android.os.Bundle)} or
     * {@link android.support.v4.app.Fragment#onCreate(android.os.Bundle)}
     * @param savedInstanceState
     * @param viewModelClass
     */
    public void onCreate(@Nullable Bundle savedInstanceState,
                         @Nullable Class<? extends AbstractViewModel<T>> viewModelClass) {
        // no viewmodel for this fragment
        if (viewModelClass == null) {
            mViewModel = null;
            return;
        }

        // screen (activity/fragment) created for first time, attach unique ID
        if (savedInstanceState == null) {
            mScreenId = UUID.randomUUID().toString();
        } else {
            mScreenId = savedInstanceState.getString("identifier");
        }

        // get model instance for this screen
        final ViewModelProvider.ViewModelWrapper<T> viewModelWrapper = ViewModelProvider.getInstance().getViewModel(mScreenId, viewModelClass);
        mViewModel = (R) viewModelWrapper.viewModel;

        // detect that the system has killed the app - saved instance is not null, but the model was recreated
        if (savedInstanceState != null && viewModelWrapper.wasCreated) {
            Log.d("model", "Fragment recreated by system - restoring viewmodel");
            mViewModel.restoreState(savedInstanceState);
        }
    }

    /**
     * Call from {@link android.support.v4.app.Fragment#onViewCreated(android.view.View, android.os.Bundle)}
     * or {@link android.app.Activity#onCreate(android.os.Bundle)}
     * @param view
     */
    public void initWithView(@NonNull T view) {
        if (mViewModel == null) {
            //no viewmodel for this fragment
            return;
        }
        mViewModel.initWithView(view);
    }

    /**
     * Use in case this model is associated with an {@link android.support.v4.app.Fragment}
     * Call from {@link android.support.v4.app.Fragment#onDestroyView()}. Use in case model is associated
     * with Fragment
     * @param fragment
     */
    public void onDestroyView(@NonNull Fragment fragment) {
        if (mViewModel == null) {
            //no viewmodel for this fragment
            return;
        }
        mViewModel.clearView();
        if (fragment.getActivity() != null && fragment.getActivity().isFinishing()) {
            removeViewModel();
        }
    }

    /**
     * Use in case this model is associated with an {@link android.support.v4.app.Fragment}
     * Call from {@link android.support.v4.app.Fragment#onDestroy()}
     * @param fragment
     */
    public void onDestroy(@NonNull Fragment fragment) {
        if (mViewModel == null) {
            //no viewmodel for this fragment
            return;
        }
        if (fragment.getActivity().isFinishing()) {
            removeViewModel();
        } else if (fragment.isRemoving()) {
            Log.d("mode", "Removing viewmodel - fragment replaced");
            removeViewModel();
        }
    }

    /**
     * Use in case this model is associated with an {@link android.app.Activity}
     * Call from {@link android.app.Activity#onDestroy()}
     * @param activity
     */
    public void onDestroy(@NonNull Activity activity) {
        if (mViewModel == null) {
            //no viewmodel for this fragment
            return;
        }
        mViewModel.clearView();
        if (activity.isFinishing()) {
            removeViewModel();
        }
    }

    /**
     * Call from {@link android.app.Activity#onStop()} or {@link android.support.v4.app.Fragment#onStop()}
     */
    public void onStop() {
        if (mViewModel == null) {
            //no viewmodel for this fragment
            return;
        }
        mViewModel.onStop();
    }

    /**
     * Call from {@link android.app.Activity#onStart()} ()} or {@link android.support.v4.app.Fragment#onStart()} ()}
     */
    public void onStart() {
        if (mViewModel == null) {
            //no viewmodel for this fragment
            return;
        }
        mViewModel.onStart();
    }


    @Nullable
    public R getViewModel() {
        return mViewModel;
    }

    /**
     * Call from {@link android.app.Activity#onSaveInstanceState(android.os.Bundle)}
     * or {@link android.support.v4.app.Fragment#onSaveInstanceState(android.os.Bundle)}.
     * This allows the model to save its state.
     * @param bundle
     */
    public void onSaveInstanceState(@NonNull Bundle bundle) {
        bundle.putString("identifier", mScreenId);
        if (mViewModel != null) {
            mViewModel.saveState(bundle);
        }
    }

    private boolean removeViewModel() {
        if (!mModelRemoved) {
            boolean removed = ViewModelProvider.getInstance().remove(mScreenId);
            mViewModel.onModelRemoved();
            mModelRemoved = true;
            return removed;
        } else {
            return false;
        }
    }
}
