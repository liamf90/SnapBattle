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
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.ui.FacebookLoginFragment


/**
 * Adapter for the [RecyclerView] in [ViewCommentsFragment].
 */
class CommentsListAdapter :
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
            //bind(createOnClickListener(plant.plantId), comment)
            bind(comment)
            itemView.tag = comment
            //TODO CHANGE cached identity idTO DAGGER
            if (comment.cognitoIdCommenter == FacebookLoginFragment.getCredentialsProvider(holder.itemView.context).cachedIdentityId){
                holder.itemView.setOnLongClickListener(getOnLongClickListenerOwnComment())
            } else{
                holder.itemView.setOnLongClickListener(getOnLongClickListenerNotOwnComment())
            }

        }
    }

    class ViewHolder(
            private val binding: ListItemCommentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Comment) {
            with(binding) {
                comment = item

                //clickListener = listener
                executePendingBindings()
            }
        }
    }
}


   private fun getOnLongClickListenerOwnComment(): View.OnLongClickListener {
       return View.OnLongClickListener {
           //Creating the instance of PopupMenu
           val popup = PopupMenu(it.context, it)
           //Inflating the Popup using xml file
           popup.getMenuInflater().inflate(R.menu.comment_delete_popup_menu, popup.getMenu())
           //registering popup with OnMenuItemClickListener
           popup.setOnMenuItemClickListener {
               //TODO DELETE COMMENT
               //deleteComment(commentsList.get(position).getCommentId())
               true
           }
           popup.show()//showing popup menu
           true
       }
   }

    private fun getOnLongClickListenerNotOwnComment(): View.OnLongClickListener {
    return View.OnLongClickListener {
        //Creating the instance of PopupMenu
        val popup = PopupMenu(it.context, it)
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.comment_report_popup_menu, popup.getMenu())
        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener {
            //TODO REPORT COMMENT
            //reportComment(commentsList.get(position).getCommentId(), position);
            true
        }
        popup.show()//showing popup menu
        true
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