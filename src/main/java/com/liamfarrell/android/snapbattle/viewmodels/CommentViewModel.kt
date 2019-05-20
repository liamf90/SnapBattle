package com.liamfarrell.android.snapbattle.viewmodels

import androidx.lifecycle.MutableLiveData
import com.liamfarrell.android.snapbattle.data.CommentRepository
import com.liamfarrell.android.snapbattle.model.Comment

/**
 * The ViewModel used in [ViewCommentFragment].
 */
class CommentViewModel(val commentRepository : CommentRepository, battleID: Int ) : ViewModelLaunch() {

    val comments = MutableLiveData<MutableList<Comment>>()


    init {
        AWSFunctionCall(true,
                suspend {comments.value = commentRepository.getComments(battleID).result.sql_result})
    }

    fun deleteComment(commentID: Int) {
        AWSFunctionCall(false,
                suspend { val result = commentRepository.deleteComment(commentID)
                    if (result.result.affectedRows == 1) {
                        val deletedComment = comments.value?.find { commentID == commentID }
                        deletedComment?.let {  comments.value?.remove(it) }
                    }
                }
        )
    }



}