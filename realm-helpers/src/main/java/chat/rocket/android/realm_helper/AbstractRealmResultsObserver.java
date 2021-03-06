package chat.rocket.android.realm_helper;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;

abstract class AbstractRealmResultsObserver<T extends RealmObject> {
  protected Realm realm;

  private RealmChangeListener<RealmResults<T>> listener;

  protected abstract RealmResults<T> queryItems(Realm realm);

  protected abstract RealmChangeListener<RealmResults<T>> getListener();

  private RealmResults<T> results;
  private final RealmHelper helper;

  protected AbstractRealmResultsObserver(RealmHelper helper) {
    this.helper = helper;
  }

  public void sub() {
    unsub();

    realm = helper.instance();
    listener = getListener();
    results = queryItems(realm);

    listener.onChange(results);
    results.addChangeListener(listener);
  }

  public void unsub() {
    if (realm != null) {
      if (results != null) {
        if (results.isValid()) {
          results.removeChangeListener(listener);
        }
        results = null;
      }
      realm.close();
      realm = null;
    }
  }

}
