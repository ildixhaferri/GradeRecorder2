package com.aim.graderecorder2.adapters;



import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aim.graderecorder2.Constants;
import com.aim.graderecorder2.R;
import com.aim.graderecorder2.fragments.AssignmentListFragment;
import com.aim.graderecorder2.models.Assignment;
import com.aim.graderecorder2.utils.Utils;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;

import java.util.ArrayList;
import java.util.Collections;


public final class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.ViewHolder> {

    private final AssignmentListFragment.OnAssignmentSelectedListener mAssignmentSelectedListener;
    private String mCourseKey;
    private Firebase mAssignmentsRef;
    private ArrayList<Assignment> mAssignments = new ArrayList<>();
    private AssignmentListFragment mAssignmentListFragment;

    public AssignmentAdapter(AssignmentListFragment assignmentListFragment,
                             String courseKey,
                             AssignmentListFragment.OnAssignmentSelectedListener assignmentSelectedListener) {
        mAssignmentListFragment = assignmentListFragment;
        mAssignmentSelectedListener = assignmentSelectedListener;

        mCourseKey = courseKey;
        mAssignmentsRef = new Firebase(Constants.ASSIGNMENTS_PATH);
        Query assignmentsForCourseRef = mAssignmentsRef.orderByChild(Assignment.COURSE_KEY).equalTo(courseKey);
        assignmentsForCourseRef.addChildEventListener(new AssignmentsChildEventListener());
    }

    public void firebasePush(final String name, final double maxGrade) {
        Assignment assignment = new Assignment(mCourseKey, name, maxGrade);
        Firebase assignmentRef = mAssignmentsRef.push();
        String assignmentKey = assignmentRef.getKey();
        assignmentRef.setValue(assignment);

        // TODO: Create grade entries also!
        Utils.createGradeEntriesForAssignment(mCourseKey, assignmentKey);

    }

    public void firebaseEdit(Assignment assignment, String assignmentName, double assignmentMaxGrade) {
        assignment.setName(assignmentName);
        assignment.setMaxGrade(assignmentMaxGrade);
        mAssignmentsRef.child(assignment.getKey()).setValue(assignment);
    }

    public void firebaseRemove(Assignment assignmentToRemove) {
        mAssignmentsRef.child(assignmentToRemove.getKey()).removeValue();
    }

    @Override
    public AssignmentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_view_text, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AssignmentAdapter.ViewHolder holder, int position) {
        holder.mAssignmentNameTextView.setText(mAssignments.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return mAssignments.size();
    }

    private class AssignmentsChildEventListener implements ChildEventListener {
        private void add(DataSnapshot dataSnapshot) {
            Assignment assignment = dataSnapshot.getValue(Assignment.class);
            assignment.setKey(dataSnapshot.getKey());
            mAssignments.add(assignment);
            Collections.sort(mAssignments);
        }

        private int remove(String key) {
            for (Assignment a : mAssignments) {
                if (a.getKey().equals(key)) {
                    int foundPos = mAssignments.indexOf(a);
                    mAssignments.remove(a);
                    return foundPos;
                }
            }
            return -1;
        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d(Constants.TAG, "Data snapshot: " + dataSnapshot);
            add(dataSnapshot);
            // We think using notifyItemInserted can cause crashes due to animation race condition.
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
        public void onCancelled(FirebaseError firebaseError) {
            Log.e("TAG", "Error: " + firebaseError.getMessage());
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private TextView mAssignmentNameTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mAssignmentNameTextView = (TextView) itemView.findViewById(R.id.text_view);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // TODO: go to grade entry
            Assignment assignment = mAssignments.get(getAdapterPosition());
            mAssignmentSelectedListener.onAssignmentSelected(assignment);
        }

        @Override
        public boolean onLongClick(View v) {
            Assignment assignment = mAssignments.get(getAdapterPosition());
            mAssignmentListFragment.showAssignmentDialog(assignment);
            return true;
        }
    }
}
