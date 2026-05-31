#!/usr/bin/env bash

export PGPASSWORD=""

psql -U postgres -h localhost -d space_mission <<'SQL'
SELECT format(
    'SELECT ''===== %I ====='' AS header; TABLE %I;',
    tablename,
    tablename
)
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY tablename
\gexec
SQL
