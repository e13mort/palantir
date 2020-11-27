package com.e13mort.gitlab_report.model

class UnsupportedRepositoryOperationException(operation: String) :
    UnsupportedOperationException("Operation $operation isn't supported")