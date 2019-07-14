package com.liamfarrell.android.snapbattle.db

import androidx.room.TypeConverter
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.ui.createbattle.ChooseVotingFragment
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


}