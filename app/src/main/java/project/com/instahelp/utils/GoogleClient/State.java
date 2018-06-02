package utils.GoogleClient;

public enum State {
    CREATED {
        @Override
        void connect(GoogleConnection googleConnection) {
            googleConnection.onSignUp();
        }

        @Override
        void disconnect(GoogleConnection googleConnection) {
            googleConnection.onSignOut();
        }
    },
    OPENING {
    },
    OPENED {
        @Override
        void disconnect(GoogleConnection googleConnection) {
            googleConnection.onSignOut();
        }

        @Override
        void revokeAccessAndDisconnect(GoogleConnection googleConnection) {
            googleConnection.onRevokeAccessAndDisconnect();
        }
    },
    CLOSED {
        @Override
        void connect(GoogleConnection googleConnection) {
            googleConnection.onSignIn();
        }
    };

    void connect(GoogleConnection googleConnection) {
        googleConnection.onSignIn();
    }

    void disconnect(GoogleConnection googleConnection) {
    }

    void revokeAccessAndDisconnect(GoogleConnection googleConnection) {
    }
}
