package si.fri.ais.session.api.cli.actions;

import si.fri.ais.session.api.cli.exceptions.SessionCliException;
import si.fri.ais.session.core.models.SessionSlot;

import java.util.UUID;

public interface SessionCliResolver {
    SessionSlot slot(boolean global, String name);

    String sessionName(String name);

    UUID userId(String name, boolean keepCurrent);

    void requireInteractive(SessionCliException.Code code);

    String chooseNamedSession();

    void reportSelectionChanged();

    void validateWorkspaceOption(String name);

    void promptToKeepWorkspace();
}
