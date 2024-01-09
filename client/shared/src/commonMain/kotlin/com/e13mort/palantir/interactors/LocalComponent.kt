package com.e13mort.palantir.interactors

import com.e13mort.palantir.model.local.DBMergeRequestRepository
import com.e13mort.palantir.model.local.DBNotesRepository
import com.e13mort.palantir.model.local.DBProjectRepository
import com.e13mort.palantir.model.local.LocalModel

class LocalComponent(model: LocalModel) {
    val projectRepository = DBProjectRepository(model)
    val mrRepository = DBMergeRequestRepository(model)
    val notesRepository = DBNotesRepository(model)
}