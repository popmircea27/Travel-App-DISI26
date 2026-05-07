CREATE TABLE IF NOT EXISTS friendships (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    requester_id UUID NOT NULL,
    receiver_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL,
    CONSTRAINT fk_friendship_requester FOREIGN KEY (requester_id) REFERENCES users(id),
    CONSTRAINT fk_friendship_receiver FOREIGN KEY (receiver_id) REFERENCES users(id),
    CONSTRAINT chk_friendship_different_users CHECK (requester_id <> receiver_id)
);

CREATE INDEX IF NOT EXISTS idx_friendships_requester_id ON friendships(requester_id);
CREATE INDEX IF NOT EXISTS idx_friendships_receiver_id ON friendships(receiver_id);
CREATE INDEX IF NOT EXISTS idx_friendships_status ON friendships(status);

CREATE UNIQUE INDEX IF NOT EXISTS ux_friendships_pair ON friendships (
    LEAST(requester_id, receiver_id),
    GREATEST(requester_id, receiver_id)
);
