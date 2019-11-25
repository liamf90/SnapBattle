package com.liamfarrell.android.snapbattle.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.liamfarrell.android.snapbattle.databinding.ListItemCommentBinding
import com.liamfarrell.android.snapbattle.model.Comment
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.findNavController
import com.amazonaws.mobile.auth.core.IdentityManager
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.mvvm_ui.ViewCommentsFragmentDirections
import com.liamfarrell.android.snapbattle.viewmodels.CommentViewModel


/**
 * Adapter for the [RecyclerView] in [ViewCommentsFragment].
 */
class CommentsListAdapter (private  val viewModel : CommentViewModel, val viewHolderOnClick : (String) -> Unit ) :
        ListAdapter<Comment, CommentsListAdapter.ViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_comment, parent, false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = getItem(position)
        holder.apply {
            bind(comment,  getProfilePicOnClickListener(comment.cognitoIdCommenter))
            itemView.tag = comment
            holder.itemView.setOnClickListener{viewHolderOnClick(comment.username)}
            if (comment.cognitoIdCommenter == IdentityManager.getDefaultIdentityManager().getCachedUserID()){
                holder.itemView.setOnLongClickListener(getOnLongClickListenerOwnComment(comment.commentId))
            } else{
                holder.itemView.setOnLongClickListener(getOnLongClickListenerNotOwnComment(comment.commentId))
            }

        }
    }

    private fun getProfilePicOnClickListener(cognitoIdOpponent: String): View.OnClickListener {
        return View.OnClickListener {
            //go to user
            val direction = ViewCommentsFragmentDirections.actionViewCommentsFragment3ToUsersBattlesFragment(cognitoIdOpponent)
            it.findNavController().navigate(direction)
        }
    }

    private fun getOnLongClickListenerOwnComment(commentID: Int): View.OnLongClickListener {
        return View.OnLongClickListener {
            //Creating the instance of PopupMenu
            val popup = PopupMenu(it.context, it)
            //Inflating the Popup using xml file
            popup.getMenuInflater().inflate(R.menu.comment_delete_popup_menu, popup.getMenu())
            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener {
                viewModel.deleteComment(commentID)
                true
            }
            popup.show()//showing popup menu
            true
        }
    }

    private fun getOnLongClickListenerNotOwnComment(commentID: Int): View.OnLongClickListener {
        return View.OnLongClickListener {
            //Creating the instance of PopupMenu
            val popup = PopupMenu(it.context, it)
            //Inflating the Popup using xml file
            popup.getMenuInflater().inflate(R.menu.comment_report_popup_menu, popup.getMenu())
            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener {
                viewModel.reportComment(commentID)
                true
            }
            popup.show()//showing popup menu
            true
        }
    }


    class ViewHolder(
            private val binding: ListItemCommentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Comment, profilePicOnClickListener: View.OnClickListener){
            with(binding) {
                comment = item
                this.profilePicOnClickListener = profilePicOnClickListener
                executePendingBindings()
            }
        }
    }
}


private class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {

    override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem.commentId == newItem.commentId
    }

    override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem == newItem
    }
}