SELECT lt.*,
  CASE WHEN i.instance_id is NULL THEN '' ELSE i.instance_id END as instance_id,
  CASE WHEN o.id is NULL THEN '' ELSE o.id END as orgunit_id
FROM %s lt
  LEFT join instance_tracker i ON lt.patient_identifier = i.patient_id
  LEFT join orgunit_tracker o ON lt.org_unit = o.orgUnit
  WHERE lt.date_created::TIMESTAMP > COALESCE((SELECT last_synced_date
                                    FROM marker
                                    WHERE category='instance' AND program_name='%s'), '-infinity');
