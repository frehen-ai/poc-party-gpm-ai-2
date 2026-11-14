package no.frehen.pocpartygpm.domain.status;

/**
 * Reference status from {@code ref_status}, with the party types it is valid for.
 */
public record RefStatus(
    String code,
    String name,
    String gkCode,
    String gkName,
    boolean validForPerson,
    boolean validForOrganisation,
    boolean validForMultiCustomer
) {
}
