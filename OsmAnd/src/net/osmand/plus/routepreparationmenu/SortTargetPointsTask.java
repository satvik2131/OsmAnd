package net.osmand.plus.routepreparationmenu;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import net.osmand.Location;
import net.osmand.TspAnt;
import net.osmand.data.LatLon;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.helpers.TargetPointsHelper;
import net.osmand.plus.helpers.TargetPointsHelper.TargetPoint;
import net.osmand.plus.helpers.WaypointDialogHelper;
import net.osmand.plus.utils.AndroidUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class SortTargetPointsTask extends AsyncTask<Void, Void, int[]> {

	protected WeakReference<FragmentActivity> activityRef;
	private final OsmandApplication app;
	private final WaypointDialogHelper helper;
	private ProgressDialog dlg;
	private long startDialogTime;
	private List<TargetPoint> intermediates;

	public SortTargetPointsTask(@NonNull FragmentActivity activity, @NonNull WaypointDialogHelper helper) {
		app = (OsmandApplication) activity.getApplicationContext();
		activityRef = new WeakReference<>(activity);
		this.helper = helper;
	}

	protected void onPreExecute() {
		startDialogTime = System.currentTimeMillis();
		FragmentActivity activity = activityRef.get();
		if (AndroidUtils.isActivityNotDestroyed(activity)) {
			dlg = new ProgressDialog(activity);
			dlg.setTitle("");
			dlg.setMessage(app.getString(R.string.intermediate_items_sort_by_distance));
			dlg.show();
		}
	}

	protected int[] doInBackground(Void[] params) {
		TargetPointsHelper targets = app.getTargetPointsHelper();
		intermediates = targets.getIntermediatePointsWithTarget();

		Location cll = app.getLocationProvider().getLastKnownLocation();
		ArrayList<TargetPoint> lt = new ArrayList<>(intermediates);
		TargetPoint start;

		if (cll != null) {
			LatLon ll = new LatLon(cll.getLatitude(), cll.getLongitude());
			start = TargetPoint.create(ll, null);
		} else if (app.getTargetPointsHelper().getPointToStart() != null) {
			TargetPoint ps = app.getTargetPointsHelper().getPointToStart();
			LatLon ll = new LatLon(ps.getLatitude(), ps.getLongitude());
			start = TargetPoint.create(ll, null);
		} else {
			start = lt.get(0);
		}
		TargetPoint end = lt.remove(lt.size() - 1);
		ArrayList<LatLon> al = new ArrayList<>();
		for (TargetPoint p : lt) {
			al.add(p.point);
		}
		try {
			return new TspAnt().readGraph(al, start.point, end.point).solve();
		} catch (Exception e) {
			return null;
		}
	}

	protected void onPostExecute(int[] result) {
		if (dlg != null) {
			long t = System.currentTimeMillis();
			if (t - startDialogTime < 500) {
				app.runInUIThread(() -> dlg.dismiss(), 500 - (t - startDialogTime));
			} else {
				dlg.dismiss();
			}
		}
		if (result == null) {
			return;
		}
		List<TargetPoint> alocs = new ArrayList<>();
		for (int i : result) {
			if (i > 0) {
				TargetPoint loc = intermediates.get(i - 1);
				alocs.add(loc);
			}
		}
		intermediates.clear();
		intermediates.addAll(alocs);

		TargetPointsHelper targets = app.getTargetPointsHelper();
		List<TargetPoint> cur = targets.getIntermediatePointsWithTarget();
		boolean eq = true;
		for (int j = 0; j < cur.size() && j < intermediates.size(); j++) {
			if (cur.get(j) != intermediates.get(j)) {
				eq = false;
				break;
			}
		}
		if (!eq) {
			targets.reorderAllTargetPoints(intermediates, true);
		}
		helper.notifyReloadAdapter();
		FragmentActivity activity = activityRef.get();
		if (AndroidUtils.isActivityNotDestroyed(activity)) {
			WaypointDialogHelper.updateRouteInfoMenu(activity);
		}
	}
}
