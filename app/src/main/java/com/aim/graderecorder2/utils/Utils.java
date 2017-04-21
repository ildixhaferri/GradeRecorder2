package com.aim.graderecorder2.utils;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.aim.graderecorder2.Constants;
import com.aim.graderecorder2.models.Assignment;
import com.aim.graderecorder2.models.Course;
import com.aim.graderecorder2.models.Owner;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


/**
 * Created by Matt Boutell on 10/2/2015. Utility class that handles bookkeeping and one-off
 * access to Firebase.
 */
public class Utils {
    public static void removeCourse(Context context, Course course) {
        // MB: Moved to first to try to speed up UI. Test for race conditions.
        // Removes from list of courses
        DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference(Constants.COURSES_PATH + "/" + course.getKey());
        courseRef.removeValue();

        // Remove this course from all its owners.
        DatabaseReference ownersRef = FirebaseDatabase.getInstance().getReference(Constants.OWNERS_PATH);
        for (String uid : course.getOwners().keySet()) {
            ownersRef.child(uid).child(Owner.COURSES).child(course.getKey()).removeValue();
        }

        // CONSIDER: Remove all students associated with this course


        // Remove all assignments
        final DatabaseReference assignmentsRef =  FirebaseDatabase.getInstance().getReference((Constants.ASSIGNMENTS_PATH));
        Query assignmentsForCourseRef = assignmentsRef.orderByChild(Assignment.COURSE_KEY).equalTo(course.getKey());
        assignmentsForCourseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    assignmentsRef.child(snapshot.getKey()).removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.d(Constants.TAG, "Cancelled");
            }
        });

        // CONSIDER: Remove all grade entries associated with this course


        // Remove from SharedPrefs
        // MB: CONSIDER What if we aren't removing the current course?
        SharedPreferencesUtils.removeCurrentCourseKey(context);
    }

    public static void signOut(Context context) {
        // Remove UID and active course from shared prefs
        SharedPreferencesUtils.removeCurrentCourseKey(context);
        SharedPreferencesUtils.removeCurrentUser(context);

        // Log out
        FirebaseAuth mAuth  = FirebaseAuth.getInstance();
        mAuth.signOut();
    }


    public static void createGradeEntriesForAssignment(String courseKey, final String assignmentKey) {
        // TODO: Loop over students in this course, and when hear about one, create and push a GradeEntry for it.
        // A childEventListener should work fine

    }

    public static void getCurrentCourseNameForToolbar(final FragmentWithToolbar fragment) {
        String currentCourseKey = SharedPreferencesUtils.getCurrentCourseKey(((Fragment) fragment).getContext());

        // If no course, then flag it as such
        if (currentCourseKey == null || currentCourseKey.isEmpty()) {
            fragment.setToolbarTitle("");
        }

        // Otherwise, get the course name from Firebase
        DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference((Constants.COURSES_PATH + "/" + currentCourseKey));
        Log.d(Constants.TAG, "Adding listener for course key: " + currentCourseKey + " for path " + courseRef.child("name").toString());
        courseRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String title = (String) dataSnapshot.getValue();
                fragment.setToolbarTitle(title);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                // empty
            }
        });
    }

    public interface FragmentWithToolbar {
        void setToolbarTitle(String courseName);
    }
}
