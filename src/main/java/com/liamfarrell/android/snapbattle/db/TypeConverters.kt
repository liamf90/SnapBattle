package com.liamfarrell.android.snapbattle.db

import androidx.room.TypeConverter
import com.liamfarrell.android.snapbattle.notifications.NotificationType
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.notifications.VotingCompleteNotification
import com.liamfarrell.android.snapbattle.mvvm_ui.create_battle.ChooseVotingFragment
import java.util.*


/**
 * Type converters to allow Room to reference complex data types.
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun whoTurnToString(whoTurn: Battle.Who_turn?) : String?{
        return if (whoTurn == null) null else whoTurn.name
    }

    @TypeConverter
    fun stringToWhoTurn(whoTurnString: String?): Battle.Who_turn?{
        return if (whoTurnString == null) null else Battle.Who_turn.valueOf(whoTurnString)
    }

    @TypeConverter
    fun votingChoiceToString(votingChoice: ChooseVotingFragment.VotingChoice?) : String?{
        return if (votingChoice == null) null else votingChoice.name
    }

    @TypeConverter
    fun stringToVotingChoice(votingChoiceString: String?): ChooseVotingFragment.VotingChoice?{
        return if (votingChoiceString == null) null else  ChooseVotingFragment.VotingChoice.valueOf(votingChoiceString)
    }

    @TypeConverter
    fun votingLengthToString(votingChoice: ChooseVotingFragment.VotingLength?) : String?{
        return if (votingChoice == null) null else votingChoice.name
    }

    @TypeConverter
    fun stringToVotingLength(votingLengthString: String?): ChooseVotingFragment.VotingLength?{
        return if (votingLengthString == null) null else  ChooseVotingFragment.VotingLength.valueOf(votingLengthString)
    }

    @TypeConverter
    fun notificationTypeToString(notificationType: NotificationType?) : String?{
        return if (notificationType == null) null else notificationType.name
    }

    @TypeConverter
    fun stringToNotificationType(notificationTypeString: String?): NotificationType?{
        return if (notificationTypeString == null) null else  NotificationType.valueOf(notificationTypeString)
    }

    @TypeConverter
    fun votingResultToString(votingResult: VotingCompleteNotification.VotingResult?) : String?{
        return if (votingResult == null) null else votingResult.name
    }

    @TypeConverter
    fun stringToVotingResult(votingResultString: String?): VotingCompleteNotification.VotingResult?{
        return if (votingResultString == null) null else  VotingCompleteNotification.VotingResult.valueOf(votingResultString)
    }



}