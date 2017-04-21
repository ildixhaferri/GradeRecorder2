package com.aim.graderecorder2.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aim.graderecorder2.Constants;
import com.aim.graderecorder2.R;
import com.aim.graderecorder2.fragments.CourseListFragment;
import com.aim.graderecorder2.models.Course;
import com.aim.graderecorder2.models.Owner;
import com.aim.graderecorder2.utils.SharedPreferencesUtils;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;



public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

    private final CourseListFragment mCourseListFragment;

    private final CourseListFragment.OnCourseSelectedListener mCourseSelectedListener;
    private String mUid;
    private DatabaseReference mOwnerRef;
    private DatabaseReference mCoursesRef;
    private ArrayList<Course> mCourses = new ArrayList<>();

    public CourseAdapter(CourseListFragment courseListFragment, CourseListFragment.OnCourseSelectedListener listener) {
        Log.d(Constants.TAG, "CourseAdapter adding OwnerValueListener");

        mCourseListFragment = courseListFragment;
        mCourseSelectedListener = listener;

        mUid = SharedPreferencesUtils.getCurrentUser(courseListFragment.getContext());
        Log.d(Constants.TAG, "Current user: " + mUid);

        assert (!mUid.isEmpty()); // Consider: use if (BuildConfig.DEBUG)

        mCoursesRef = FirebaseDatabase.getInstance().getReference(Constants.COURSES_PATH);
        // Deep query. Find the courses owned by me
        Query query = mCoursesRef.orderByChild("owners/" + mUid).equalTo(true);
        query.addChildEventListener(new CoursesChildEventListener());

        // This is so that a new course can be pushed to the onwers path as well.
        mOwnerRef = FirebaseDatabase.getInstance().getReference(Constants.OWNERS_PATH + "/" + mUid);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_view_text, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mCourseNameTextView.setText(mCourses.get(position).getName());
    }

    public void firebasePush(String courseName) {
        // Create a new auto-ID for a course in the courses path
        DatabaseReference ref = mCoursesRef.push();
        // Add the course to the courses path
        ref.setValue(new Course(courseName, mUid));

        // Add the course to the owners path
        Map<String, Object> map = new HashMap<>();
        map.put(ref.getKey(), true);
        // See https://www.firebase.com/docs/android/guide/saving-data.html for this method.
        mOwnerRef.child(Owner.COURSES).updateChildren(map);
    }

    public void firebaseEdit(Course course, String newCourseName) {
        // Since there is only 1 editable field, we set it directly by tunneling down the path 1 more level.
        DatabaseReference courseNameRef = FirebaseDatabase.getInstance().getReference(Constants.COURSES_PATH + "/" + course.getKey() + "/" + Course.NAME);
        courseNameRef.setValue(newCourseName);
    }

    // Where is firebaseRemove? It is a Utils method since removing a course cascades to every table in the Firebase.


    @Override
    public int getItemCount() {
        return mCourses.size();
    }

    class CoursesChildEventListener implements ChildEventListener {
        // While we don't push up deletes, we need to listen for other owners deleting our course.

        private void add(DataSnapshot dataSnapshot) {
            Course course = dataSnapshot.getValue(Course.class);
            course.setKey(dataSnapshot.getKey());
            mCourses.add(course);
            Collections.sort(mCourses);
        }

        private int remove(String key) {
            for (Course course : mCourses) {
                if (course.getKey().equals(key)) {
                    int foundPos = mCourses.indexOf(course);
                    mCourses.remove(course);
                    return foundPos;
                }
            }
            return -1;
        }


        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d(Constants.TAG, "My course: " + dataSnapshot);
            add(dataSnapshot);
            notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            remove(dataSnapshot.getKey());
            add(dataSnapshot);
            notifyDataSetChanged();
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            int position = remove(dataSnapshot.getKey());
            if (position >= 0) {
                notifyItemRemoved(position);
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            // empty
        }

        @Override
        public void onCancelled(DatabaseError firebaseError) {
            Log.e("TAG", "onCancelled. Error: " + firebaseError.getMessage());

        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private TextView mCourseNameTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mCourseNameTextView = (TextView) itemView.findViewById(R.id.text_view);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            SharedPreferencesUtils.setCurrentCourseKey(mCourseListFragment.getContext(), mCourses.get(getAdapterPosition()).getKey());
            Course course = mCourses.get(getAdapterPosition());
            mCourseSelectedListener.onCourseSelected(course);
        }

        @Override
        public boolean onLongClick(View v) {
            Course course = mCourses.get(getAdapterPosition());
            mCourseListFragment.showCourseDialog(course);
            return true;
        }
    }
}
