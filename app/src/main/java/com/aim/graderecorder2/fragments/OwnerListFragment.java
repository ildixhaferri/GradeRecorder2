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
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.aim.graderecorder2.GradeRecorderActivity;
import com.aim.graderecorder2.R;
import com.aim.graderecorder2.adapters.OwnerAdapter;
import com.aim.graderecorder2.models.Owner;
import com.aim.graderecorder2.utils.SharedPreferencesUtils;
import com.aim.graderecorder2.utils.Utils;


public class OwnerListFragment extends Fragment implements Utils.FragmentWithToolbar {
    private Toolbar mToolbar;
    private OwnerAdapter mAdapter;
    private OnThisOwnerRemovedListener mListener;

    public OwnerListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_owner_list, container, false);

        final FloatingActionButton fab = ((GradeRecorderActivity) getActivity()).getFab();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOwnerDialog();
            }
        });
        fab.setVisibility(View.VISIBLE);

        mToolbar = ((GradeRecorderActivity) getActivity()).getToolbar();
        mToolbar.setTitle("Owners");
        Utils.getCurrentCourseNameForToolbar(this);

        String currentCourseKey = SharedPreferencesUtils.getCurrentCourseKey(getContext());

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.owner_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        mAdapter = new OwnerAdapter(this, currentCourseKey, mListener);
        recyclerView.setAdapter(mAdapter);
        return rootView;
    }

    @Override
    public void setToolbarTitle(String courseName) {
        if (courseName == null || courseName.isEmpty()) {
            mToolbar.setTitle("Choose a course first");
        } else {
            mToolbar.setTitle("Owners of " + courseName);
        }
    }

    @SuppressLint("InflateParams")
    private void showOwnerDialog() {
        DialogFragment df = new DialogFragment() {
            @NonNull
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.dialog_owner_add_title);

                View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add, null, false);
                builder.setView(view);
                final EditText ownerNameEditText = (EditText) view.findViewById(R.id.dialog_add_data);
                ownerNameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                ownerNameEditText.setHint(R.string.dialog_owner_hint);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String ownerName = ownerNameEditText.getText().toString();
                        mAdapter.addOwner(ownerName);
                        dismiss();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                return builder.create();
            }
        };
        df.show(getActivity().getSupportFragmentManager(), "add_owner_dialog");
    }

    public void showRemoveOwnerDialog(final Owner owner) {
        android.app.DialogFragment df = new android.app.DialogFragment() {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.dialog_owner_remove_title);
                builder.setMessage(getString(R.string.dialog_owner_remove_message_format, owner.getUsername()));
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAdapter.removeOwner(owner);
                        dismiss();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                return builder.create();
            }
        };
        df.show(getActivity().getFragmentManager(), "remove_owner_dialog");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnThisOwnerRemovedListener) {
            mListener = (OnThisOwnerRemovedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnThisOwnerRemovedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnThisOwnerRemovedListener {
        void onThisOwnerRemoved();
    }
}
