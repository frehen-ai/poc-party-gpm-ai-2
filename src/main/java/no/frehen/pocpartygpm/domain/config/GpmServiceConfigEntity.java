package no.frehen.pocpartygpm.domain.config;

/**
 * Simple key/value configuration entity.
 */
public record GpmServiceConfigEntity(
    Long id,
    String key,
    String value
) {
}
