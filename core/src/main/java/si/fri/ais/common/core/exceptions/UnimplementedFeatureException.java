package si.fri.ais.common.core.exceptions;

public class UnimplementedFeatureException extends RuntimeException {
    public UnimplementedFeatureException(String feature) {
        super("%s is not yet implemented.".formatted(feature));
    }
}
