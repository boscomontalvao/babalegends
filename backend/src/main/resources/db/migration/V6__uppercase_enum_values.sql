-- V6__uppercase_enum_values.sql
-- Os enums Java (@Enumerated(EnumType.STRING)) esperam os nomes em maiusculo:
-- ADMIN/MEMBER, SCHEDULED/CANCELLED/COMPLETED, PENDING/CONFIRMED/DECLINED.
-- Corrige os dados existentes e os DEFAULT das colunas.

UPDATE tb_group_member
SET role = UPPER(role);
UPDATE tb_match
SET status = UPPER(status);
UPDATE tb_attendance
SET status = UPPER(status);

ALTER TABLE tb_group_member
    ALTER COLUMN role SET DEFAULT 'MEMBER';
ALTER TABLE tb_match
    ALTER COLUMN status SET DEFAULT 'SCHEDULED';
ALTER TABLE tb_attendance
    ALTER COLUMN status SET DEFAULT 'PENDING';