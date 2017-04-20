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
import com.aim.graderecorder2.adapters.CourseAdapter;
import com.aim.graderecorder2.models.Course;
import com.aim.graderecorder2.utils.Utils;
import com.aim.graderecorder2.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnCourseSelectedListener} interface
 * to handle interaction events.
 */

public class CourseListFragment extends Fragment implements Utils.FragmentWithToolbar {
    private CourseAdapter mAdapter;

    private OnCourseSelectedListener mListener;
    private Toolbar mToolbar;

    public CourseListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Context context = getContext();

        View rootView = inflater.inflate(R.layout.fragment_course_list, container, false);

        FloatingActionButton fab = ((GradeRecorderActivity) context).getFab();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCourseDialog(null);
            }
        });
        fab.setVisibility(View.VISIBLE);

        mToolbar = ((GradeRecorderActivity) context).getToolbar();
        Utils.getCurrentCourseNameForToolbar(this);

        mToolbar.setTitle(R.string.fragment_title_course);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.course_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setHasFixedSize(true);
        registerForContextMenu(recyclerView);
        mAdapter = new CourseAdapter(this, mListener);
        recyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void setToolbarTitle(String courseName) {
        if (courseName == null || courseName.isEmpty()) {
            mToolbar.setTitle(R.string.fragment_title_course);
        } else {
            mToolbar.setTitle("Selected " + courseName);
        }
    }


    @SuppressLint("InflateParams")
    public void showCourseDialog(final Course course) {
        DialogFragment df = new DialogFragment() {
            @NonNull
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(course == null ? R.string.dialog_course_add_title : R.string.dialog_course_edit_title));

                View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add, null);
                builder.setView(view);
                final EditText courseNameEditText = (EditText) view.findViewById(R.id.dialog_add_data);
                courseNameEditText.setHint(R.string.dialog_course_hint);
                if (course != null) {
                    courseNameEditText.setText(course.getName());
                }
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String courseName = courseNameEditText.getText().toString();
                        if (course == null) {
                            mAdapter.firebasePush(courseName);
                        } else {
                            mAdapter.firebaseEdit(course, courseName);
                        }
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                if (course != null) {
                    builder.setNeutralButton(R.string.remove, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showDeleteConfirmationDialog(course);
                        }
                    });
                }
                return builder.create();
            }
        };
        df.show(getActivity().getSupportFragmentManager(), "addedit");
    }

    private void showDeleteConfirmationDialog(final Course course) {
        DialogFragment df = new DialogFragment() {
            @Override
            @NonNull
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.remove_question_format, course.getName()));
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Utils.removeCourse(getActivity(), course);
                        dismiss();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                return builder.create();
            }
        };
        df.show(getActivity().getSupportFragmentManager(), "confirm");
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCourseSelectedListener) {
            mListener = (OnCourseSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCourseSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnCourseSelectedListener {
        void onCourseSelected(Course selectedCourse);
    }
}
