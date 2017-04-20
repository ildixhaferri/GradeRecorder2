package com.aim.graderecorder2.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.aim.graderecorder2.GradeRecorderActivity;
import com.aim.graderecorder2.R;
import com.aim.graderecorder2.adapters.AssignmentAdapter;
import com.aim.graderecorder2.models.Assignment;
import com.aim.graderecorder2.utils.Utils;


public class AssignmentListFragment extends Fragment implements Utils.FragmentWithToolbar {
    private static final String ARG_CURRENT_COURSE_KEY = "CURRENT_COURSE_KEY";

    private String mCurrentCourseKey;
    private AssignmentAdapter mAdapter;
    private Toolbar mToolbar;

    private OnAssignmentSelectedListener mOnAssignmentSelectedListener;

    public AssignmentListFragment() {
        // Required empty public constructor
    }

    public static AssignmentListFragment newInstance(String currentCourseKey) {
        AssignmentListFragment fragment = new AssignmentListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CURRENT_COURSE_KEY, currentCourseKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCurrentCourseKey = getArguments().getString(ARG_CURRENT_COURSE_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_assignment_list, container, false);

        FloatingActionButton fab = ((GradeRecorderActivity) getActivity()).getFab();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAssignmentDialog(null);
            }
        });
        fab.setVisibility(View.VISIBLE);

        mToolbar = ((GradeRecorderActivity) getActivity()).getToolbar();
        Utils.getCurrentCourseNameForToolbar(this);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.assignment_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        registerForContextMenu(recyclerView);
        mAdapter = new AssignmentAdapter(this, mCurrentCourseKey, mOnAssignmentSelectedListener);
        recyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void setToolbarTitle(String courseName) {
        if (courseName == null || courseName.isEmpty()) {
            mToolbar.setTitle("Choose a course");
        } else {
            mToolbar.setTitle("Asn for " + courseName);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAssignmentSelectedListener) {
            mOnAssignmentSelectedListener = (OnAssignmentSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAssignmentSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnAssignmentSelectedListener = null;
    }

    @SuppressLint("InflateParams")
    public void showAssignmentDialog(final Assignment assignment) {

        DialogFragment df = new DialogFragment() {
            @Override
            @NonNull
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(assignment == null ? R.string.dialog_assignment_add_title : R.string.dialog_assignment_edit_title));
                View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_assignment_add_edit, null, false);
                builder.setView(view);
                final EditText assignmentNameEditText = (EditText) view.findViewById(R.id.dialog_assignment_name);
                final EditText assignmentMaxGradeEditText = (EditText) view.findViewById(R.id.dialog_assignment_max_grade);
                if (assignment != null) {
                    assignmentNameEditText.setText(assignment.getName());
                    assignmentMaxGradeEditText.setText(String.valueOf(assignment.getMaxGrade()));
                }

                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String assignmentName = assignmentNameEditText.getText().toString();
                        double assignmentMaxGrade;
                        try {
                            assignmentMaxGrade = Double.parseDouble(assignmentMaxGradeEditText.getText().toString());
                        } catch (Exception e) {
                            // This is the default for many of my assignments
                            assignmentMaxGrade = 10;
                        }

                        if (assignment == null) {
                            mAdapter.firebasePush(assignmentName, assignmentMaxGrade);
                        } else {
                            mAdapter.firebaseEdit(assignment, assignmentName, assignmentMaxGrade);
                        }
                        dismiss();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                if (assignment != null) {
                    builder.setNeutralButton(R.string.remove, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showDeleteConfirmationDialog(assignment);
                        }
                    });
                }
                return builder.create();
            }
        };
        df.show(getActivity().getSupportFragmentManager(), "add_edit_assignment");
    }

    private void showDeleteConfirmationDialog(final Assignment assignment) {
        DialogFragment df = new DialogFragment() {
            @Override
            @NonNull
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.remove_question_format, assignment.getName()));
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAdapter.firebaseRemove(assignment);
                        dismiss();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                return builder.create();
            }
        };
        df.show(getActivity().getSupportFragmentManager(), "confirm");
    }


    public interface OnAssignmentSelectedListener {
        void onAssignmentSelected(Assignment assignment);
    }
}
