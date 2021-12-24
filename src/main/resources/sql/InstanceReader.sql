SELECT p.*,
  CASE WHEN i.instance_id is NULL THEN '' ELSE i.instance_id END as instance_id
FROM patient p
  LEFT join instance_tracker i ON p.patient_identifier = i.patient_id
  WHERE patient_identifier = '%s';
