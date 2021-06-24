package com.rohitthebest.pagination_3_github_api.ui.viewHolders

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.pagination_3_github_api.R
import com.rohitthebest.pagination_3_github_api.databinding.RepoViewItemBinding
import com.rohitthebest.pagination_3_github_api.model.Repo

class RepoViewHolder(val binding: RepoViewItemBinding) : RecyclerView.ViewHolder(binding.root) {

    private var repo: Repo? = null

    init {

        binding.root.setOnClickListener {

            repo?.url?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                binding.root.context.startActivity(intent)
            }
        }
    }

    fun bind(repo: Repo?) {

        if (repo == null) {
            val resources = itemView.resources
            binding.repoName.text = resources.getString(R.string.loading)
            binding.repoDescription.visibility = View.GONE
            binding.repoLanguage.visibility = View.GONE
            binding.repoStars.text = resources.getString(R.string.unknown)
            binding.repoForks.text = resources.getString(R.string.unknown)
        } else {
            showRepoData(repo)
        }
    }

    private fun showRepoData(repo: Repo) {
        this.repo = repo
        binding.repoName.text = repo.fullName

        // if the description is missing, hide the TextView
        var descriptionVisibility = View.GONE
        if (repo.description != null) {
            binding.repoDescription.text = repo.description
            descriptionVisibility = View.VISIBLE
        }
        binding.repoDescription.visibility = descriptionVisibility

        binding.repoStars.text = repo.stars.toString()
        binding.repoForks.text = repo.forks.toString()

        // if the language is missing, hide the label and the value
        var languageVisibility = View.GONE
        if (!repo.language.isNullOrEmpty()) {
            val resources = binding.root.context.resources
            binding.repoLanguage.text = resources.getString(R.string.language, repo.language)
            languageVisibility = View.VISIBLE
        }
        binding.repoLanguage.visibility = languageVisibility
    }

    companion object {
        fun create(parent: ViewGroup): RepoViewHolder {

            val binding = RepoViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

            return RepoViewHolder(binding)
        }
    }
}