package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.Project

interface AllProjectsResult {
    fun projects(synced: Boolean): List<Project>
}