package com.aim.graderecorder2.fragments;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.aim.graderecorder2.Constants;
import com.aim.graderecorder2.GradeRecorderActivity;
import com.aim.graderecorder2.R;
import com.aim.graderecorder2.adapters.StudentAdapter;
import com.aim.graderecorder2.models.Student;
import com.aim.graderecorder2.utils.SharedPreferencesUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class StudentListFragment extends Fragment {
    private Toolbar mToolbar;
    private StudentAdapter mAdapter;
    private Student mPendingDeletionStudent;

    public StudentListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_student_list, container, false);

        final FloatingActionButton fab = ((GradeRecorderActivity) getActivity()).getFab();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showStudentDialog(null);
            }
        });
        fab.setVisibility(View.VISIBLE);

        mToolbar = ((GradeRecorderActivity) getActivity()).getToolbar();
        mToolbar.setTitle("Students");

        String currentCourseKey = SharedPreferencesUtils.getCurrentCourseKey(getContext());

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.student_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new StudentAdapter(this, currentCourseKey);
        recyclerView.setAdapter(mAdapter);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //Remove swiped item from list and notify the RecyclerView
                final int position = viewHolder.getAdapterPosition();
                mPendingDeletionStudent = mAdapter.hide(position);
                final Snackbar snackbar = Snackbar
                        .make(fab, "Student removed!", Snackbar.LENGTH_INDEFINITE)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mAdapter.undoHide(mPendingDeletionStudent, position);
                                mPendingDeletionStudent = null;
                                Snackbar snackbarRestore = Snackbar.make(fab, "Student restored!", Snackbar.LENGTH_SHORT);
                                snackbarRestore.show();
                            }
                        })
                        .setCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                if (event != Snackbar.Callback.DISMISS_EVENT_ACTION && event != Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE) {
                                    Log.d(Constants.TAG, "Student to remove: " + mPendingDeletionStudent);
                                    if (mPendingDeletionStudent != null) {
                                        mAdapter.firebaseRemove(mPendingDeletionStudent);
                                    }
                                    mPendingDeletionStudent = null;
                                }
                            }
                        });
                snackbar.setActionTextColor(ContextCompat.getColor(getContext(), R.color.white));
                ((TextView) (snackbar.getView().findViewById(android.support.design.R.id.snackbar_text))).setTextSize(16);
                ((TextView) (snackbar.getView().findViewById(android.support.design.R.id.snackbar_action))).setTextSize(20);
                snackbar.show();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);


        // Get the name of the course. See listener below.
        DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference(Constants.COURSES_PATH + "/" + currentCourseKey);
        courseRef.child("name").addListenerForSingleValueEvent(new CourseNameValueEventListener());
        Log.d(Constants.TAG, "Adding listener for course key: " + currentCourseKey + " for path " + courseRef.child("name").toString());

        return rootView;
    }

    private class CourseNameValueEventListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(Constants.TAG, "Data snapshot = " + dataSnapshot);
            mToolbar.setTitle("Students in " + dataSnapshot.getValue());
        }

        @Override
        public void onCancelled(DatabaseError firebaseError) {
            // empty
        }
    }

    @SuppressLint("InflateParams")
    public void showStudentDialog(final Student student) {

        DialogFragment df = new DialogFragment() {
            @Override
            @NonNull
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(student == null ? R.string.dialog_student_add_title : R.string.dialog_student_edit_title));
                View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_student_add_edit, null);
                builder.setView(view);
                final EditText firstNameEditText = (EditText) view.findViewById(R.id.dialog_student_first_name);
                final EditText lastNameEditText = (EditText) view.findViewById(R.id.dialog_student_last_name);
                final EditText roseUsernameEditText = (EditText) view.findViewById(R.id.dialog_student_rose_username);
                final EditText teamEditText = (EditText) view.findViewById(R.id.dialog_student_team);
                if (student != null) {
                    firstNameEditText.setText(student.getFirstName());
                    lastNameEditText.setText(student.getLastName());
                    roseUsernameEditText.setText(student.getRoseUsername());
                    teamEditText.setText(student.getTeam());
                }

                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String firstName = firstNameEditText.getText().toString();
                        String lastName = lastNameEditText.getText().toString();
                        String roseUsername = roseUsernameEditText.getText().toString();
                        String team = teamEditText.getText().toString();

                        if (student == null) {
                            mAdapter.firebasePush(firstName, lastName, roseUsername, team);
                        } else {
                            mAdapter.firebaseEdit(student, firstName, lastName, roseUsername, team);
                        }
                        dismiss();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, null);

                return builder.create();
            }
        };
        df.show(getActivity().getSupportFragmentManager(), "add_edit_student");
    }
}
