package chat.rocket.android.fragment.server_config;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import chat.rocket.android.R;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.internal.Session;
import chat.rocket.android.realm_helper.RealmObjectObserver;
import chat.rocket.android.realm_helper.RealmStore;

/**
 * Login screen.
 */
public class RetryLoginFragment extends AbstractServerConfigFragment {
  @Override protected int getLayout() {
    return R.layout.fragment_retry_login;
  }

  private RealmObjectObserver<Session> sessionObserver;

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    sessionObserver = RealmStore.get(serverConfigId)
        .createObjectObserver(Session::queryDefaultSession)
        .setOnUpdateListener(this::onRenderServerConfigSession);
  }

  @Override protected void onSetupView() {
  }

  private void onRenderServerConfigSession(Session session) {
    if (session == null) {
      return;
    }

    final String token = session.getToken();
    if (!TextUtils.isEmpty(token)) {
      final View btnRetry = rootView.findViewById(R.id.btn_retry_login);
      final View waitingView = rootView.findViewById(R.id.waiting);
      waitingView.setVisibility(View.GONE);
      btnRetry.setOnClickListener(view -> {
        view.setEnabled(false);
        waitingView.setVisibility(View.VISIBLE);

        new MethodCallHelper(getContext(), serverConfigId).loginWithToken(token)
            .continueWith(task -> {
              if (task.isFaulted()) {
                view.setEnabled(true);
                waitingView.setVisibility(View.GONE);
              }
              return null;
            });
      });
    }

    final String error = session.getError();
    final TextView txtError = (TextView) rootView.findViewById(R.id.txt_error_description);
    if (!TextUtils.isEmpty(error)) {
      txtError.setText(error);
    }
  }

  @Override public void onResume() {
    super.onResume();
    sessionObserver.sub();
  }

  @Override public void onPause() {
    sessionObserver.unsub();
    super.onPause();
  }
}
