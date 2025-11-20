package com.stepsync.data.model

import com.google.firebase.firestore.DocumentSnapshot

/**
 * Extension functions for Firestore data conversion
 */

/**
 * Convert Firestore DocumentSnapshot to User data model
 */
fun DocumentSnapshot.toUser(userId: Long): User? {
    return try {
        User(
            id = userId,
            email = getString("email") ?: return null,
            name = getString("name") ?: return null,
            age = getLong("age")?.toInt() ?: return null,
            weight = getDouble("weight")?.toFloat() ?: return null,
            height = getDouble("height")?.toFloat() ?: return null,
            fitnessGoal = getString("fitnessGoal") ?: return null,
            dailyStepGoal = getLong("dailyStepGoal")?.toInt() ?: 10000
        )
    } catch (e: Exception) {
        null
    }
}

/**
 * Convert User data model to Firestore map
 */
fun User.toFirestoreMap(): Map<String, Any> {
    return hashMapOf(
        "email" to email,
        "name" to name,
        "age" to age,
        "weight" to weight,
        "height" to height,
        "fitnessGoal" to fitnessGoal,
        "dailyStepGoal" to dailyStepGoal,
        "updatedAt" to System.currentTimeMillis()
    )
}

/**
 * Convert StepRecord data model to Firestore map
 */
fun StepRecord.toFirestoreMap(firebaseUid: String): Map<String, Any> {
    return hashMapOf(
        "userId" to firebaseUid,
        "date" to date,
        "steps" to steps,
        "distance" to distance,
        "calories" to calories,
        "timestamp" to System.currentTimeMillis()
    )
}

/**
 * Convert Activity data model to Firestore map
 */
fun Activity.toFirestoreMap(firebaseUid: String): Map<String, Any> {
    return hashMapOf(
        "userId" to firebaseUid,
        "activityType" to activityType,
        "startTime" to startTime,
        "endTime" to endTime,
        "duration" to duration,
        "distance" to distance,
        "calories" to calories,
        "steps" to steps,
        "notes" to notes
    )
}

/**
 * Convert Goal data model to Firestore map
 */
fun Goal.toFirestoreMap(firebaseUid: String): Map<String, Any> {
    return hashMapOf(
        "userId" to firebaseUid,
        "goalType" to goalType,
        "targetValue" to targetValue,
        "currentValue" to currentValue,
        "period" to period,
        "startDate" to startDate,
        "endDate" to endDate,
        "isCompleted" to isCompleted,
        "createdAt" to System.currentTimeMillis()
    )
}

/**
 * Convert Achievement data model to Firestore map
 */
fun Achievement.toFirestoreMap(firebaseUid: String): Map<String, Any> {
    return hashMapOf(
        "userId" to firebaseUid,
        "achievementType" to achievementType,
        "title" to title,
        "description" to description,
        "iconName" to iconName,
        "unlockedAt" to unlockedAt
    )
}
