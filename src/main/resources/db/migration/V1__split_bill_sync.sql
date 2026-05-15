CREATE TABLE app_users (
    remote_user_id VARCHAR(128) PRIMARY KEY,
    display_name VARCHAR(255) NOT NULL,
    email VARCHAR(320),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE split_groups (
    remote_group_id VARCHAR(64) PRIMARY KEY,
    owner_user_id VARCHAR(128) NOT NULL REFERENCES app_users(remote_user_id),
    name VARCHAR(255) NOT NULL,
    invite_code VARCHAR(32) UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE split_group_members (
    remote_member_id VARCHAR(64) PRIMARY KEY,
    remote_group_id VARCHAR(64) NOT NULL REFERENCES split_groups(remote_group_id) ON DELETE CASCADE,
    remote_user_id VARCHAR(128) NOT NULL REFERENCES app_users(remote_user_id),
    role VARCHAR(32) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    email VARCHAR(320),
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE (remote_group_id, remote_user_id)
);

CREATE TABLE split_bills (
    remote_bill_id VARCHAR(64) PRIMARY KEY,
    remote_group_id VARCHAR(64) NOT NULL REFERENCES split_groups(remote_group_id) ON DELETE CASCADE,
    description VARCHAR(500) NOT NULL,
    total_minor_unit_amount BIGINT NOT NULL,
    currency_code VARCHAR(16) NOT NULL,
    payer_remote_member_id VARCHAR(64) NOT NULL REFERENCES split_group_members(remote_member_id),
    creator_remote_member_id VARCHAR(64) NOT NULL REFERENCES split_group_members(remote_member_id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE split_bill_participants (
    remote_participant_id VARCHAR(64) PRIMARY KEY,
    remote_bill_id VARCHAR(64) NOT NULL REFERENCES split_bills(remote_bill_id) ON DELETE CASCADE,
    debtor_remote_member_id VARCHAR(64) NOT NULL REFERENCES split_group_members(remote_member_id),
    owed_minor_unit_amount BIGINT NOT NULL,
    paid_minor_unit_amount BIGINT NOT NULL DEFAULT 0,
    is_settled BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (remote_bill_id, debtor_remote_member_id)
);

CREATE TABLE split_payments (
    remote_payment_id VARCHAR(64) PRIMARY KEY,
    remote_group_id VARCHAR(64) NOT NULL REFERENCES split_groups(remote_group_id) ON DELETE CASCADE,
    remote_bill_id VARCHAR(64) NOT NULL REFERENCES split_bills(remote_bill_id) ON DELETE CASCADE,
    payer_remote_member_id VARCHAR(64) NOT NULL REFERENCES split_group_members(remote_member_id),
    receiver_remote_member_id VARCHAR(64) NOT NULL REFERENCES split_group_members(remote_member_id),
    minor_unit_amount BIGINT NOT NULL,
    currency_code VARCHAR(16) NOT NULL,
    creator_remote_member_id VARCHAR(64) NOT NULL REFERENCES split_group_members(remote_member_id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE user_devices (
    id BIGSERIAL PRIMARY KEY,
    remote_user_id VARCHAR(128) NOT NULL REFERENCES app_users(remote_user_id) ON DELETE CASCADE,
    platform VARCHAR(32) NOT NULL,
    push_token TEXT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_seen_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE (push_token)
);
