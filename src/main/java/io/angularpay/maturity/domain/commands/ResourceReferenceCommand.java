package io.angularpay.maturity.domain.commands;

public interface ResourceReferenceCommand<T, R> {

    R map(T referenceResponse);
}
