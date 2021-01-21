package com.e13mort.palantir.model

class UnsupportedRepositoryOperationException(operation: String) :
    UnsupportedOperationException("Operation $operation isn't supported")