package com.aim.graderecorder2.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.aim.graderecorder2.Constants;
import com.aim.graderecorder2.R;
import com.aim.graderecorder2.fragments.OwnerListFragment;
import com.aim.graderecorder2.models.Course;
import com.aim.graderecorder2.models.Owner;
import com.aim.graderecorder2.utils.SharedPreferencesUtils;
import com.aim.graderecorder2.utils.Utils;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;



public class OwnerAdapter extends RecyclerView.Adapter<OwnerAdapter.ViewHolder> {

    private final Context mContext;
    private final OwnerListFragment mOwnerListFragment;
    private final OwnerListFragment.OnThisOwnerRemovedListener mOnThisOwnerRemovedListener;

    private String mCourseKey;
    private Firebase mCourseRef;
    private Course mCourse;
    private Firebase mOwnersRef;
    private ArrayList<Owner> mOwners = new ArrayList<>();

    public OwnerAdapter(OwnerListFragment ownerListFragment, String courseKey, OwnerListFragment.OnThisOwnerRemovedListener onThisOwnerRemovedListener) {
        mOwnerListFragment = ownerListFragment;
        mContext = ownerListFragment.getContext();
        mCourseKey = courseKey;
        mOnThisOwnerRemovedListener = onThisOwnerRemovedListener;

        // In the current course
        mCourseRef = new Firebase(Constants.COURSES_PATH + "/" + mCourseKey);
        mCourseRef.addValueEventListener(new CourseValueEventListener());

        mOwnersRef = new Firebase(Constants.OWNERS_PATH);
    }

    public void addOwner(final String username) {
        // I want to add this owner to my course, so we get all the owners with this username.
        // There should be exactly one.
        Query thisOwnerRef = mOwnersRef.orderByChild(Owner.USERNAME).equalTo(username);

        thisOwnerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(Constants.TAG, "Owner: " + dataSnapshot);
                // Check if owner with this username exists.
                if (dataSnapshot.getValue() == null) {
                    Toast.makeText(mContext, "No owner with username " + username + " exists.", Toast.LENGTH_LONG).show();
                    return; // bail
                }

                // We loop over the children, since we are still a level too high in the path. There should only be one person with that username.
                int count = 0;
                for (DataSnapshot ownerSnapshot : dataSnapshot.getChildren()) {
                    // If already owner, don't add.
                    Owner ownerToAdd = ownerSnapshot.getValue(Owner.class);
                    ownerToAdd.setKey(ownerSnapshot.getKey());

                    if (ownerToAdd.containsCourse(mCourseKey)) {
                        Toast.makeText(mContext, "This username is already a course owner.", Toast.LENGTH_LONG).show();
                        return; // bail
                    }

                    // NOTE: Double-linked so add the course to the owner and the owner to the course.
                    mOwnersRef.child(ownerToAdd.getKey()).child(Owner.COURSES).child(mCourseKey).setValue(true);
                    mCourseRef.child(Course.OWNERS).child(ownerToAdd.getKey()).setValue(true);
                    count++;
                }

                if (count > 1) {
                    Log.e(Constants.TAG, "Error: more than one owner with the given username!");
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                // empty
            }
        });
    }

    public void removeOwner(Owner ownerToRemove) {
        // Removes owner from this course and course from this owner
        String uid = SharedPreferencesUtils.getCurrentUser(mContext);

        // If all owners have been removed, delete the course
        if (mCourse.getOwners().keySet().size() == 1) {
            assert (uid.equals(ownerToRemove.getKey()));
            Utils.removeCourse(mContext, mCourse);
        } else {
            // NOTE: Double-linked so remove both links
            mCourseRef.child(Course.OWNERS).child(ownerToRemove.getKey()).removeValue();
            mOwnersRef.child(ownerToRemove.getKey()).child(Owner.COURSES).child(mCourseKey).removeValue();
        }

        // If we are removing ourself, we should go back past this courses' list of assignments to the list of courses.
        if (uid.equals(ownerToRemove.getKey())) {
            mOnThisOwnerRemovedListener.onThisOwnerRemoved();
        }
    }

    @Override
    public int getItemCount() {
        return mOwners.size();
    }

    @Override
    public OwnerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_view_text, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(OwnerAdapter.ViewHolder holder, int position) {
        holder.mOwnerNameTextView.setText(mOwners.get(position).getUsername());
    }

    class CourseValueEventListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // Save for later
            mCourse = dataSnapshot.getValue(Course.class);
            mCourse.setKey(dataSnapshot.getKey());

            // Start with fresh list of owners so we don't add to existing ones when we add a new owner.
            mOwners.clear();
            for (DataSnapshot ownerUid : dataSnapshot.child(Course.OWNERS).getChildren()) {
                Firebase ownerRef = new Firebase(Constants.OWNERS_PATH + "/" + ownerUid.getKey());
                OwnerValueEventListener listener = new OwnerValueEventListener();
                ownerRef.addListenerForSingleValueEvent(listener);
            }
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
            Log.d(Constants.TAG, "CourseValueListener cancelled: " + firebaseError);
        }
    }

    class OwnerValueEventListener implements ValueEventListener {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(Constants.TAG, "Called value listener from OwnerAdapter with value" + dataSnapshot);
            Owner owner = dataSnapshot.getValue(Owner.class);
            owner.setKey(dataSnapshot.getKey());
            mOwners.add(0, owner);
            notifyDataSetChanged();
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
            Log.d(Constants.TAG, "OwnerValueListener cancelled: " + firebaseError);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        private TextView mOwnerNameTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mOwnerNameTextView = (TextView) itemView.findViewById(R.id.text_view);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            mOwnerListFragment.showRemoveOwnerDialog(mOwners.get(getAdapterPosition()));
            return true;
        }
    }
}
