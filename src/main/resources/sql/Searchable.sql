SELECT %s FROM %s pi
INNER JOIN marker m ON pi.date_created :: TIMESTAMP > COALESCE(m.last_synced_date, '-infinity') AND category = 'instance'
AND program_name = '%s';