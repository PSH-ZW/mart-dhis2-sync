SELECT enrTable.incident_date,
       enrTable.date_created       AS enrollment_date_created,
       enrTable.program_unique_id  AS program_unique_id,
       enrTable.program            AS enrolled_program,
       enrTable.enrollment_date    AS enr_date,
       enrTable.status             AS enrollment_status,
       enrTable.patient_identifier AS enrolled_patient_identifier,
       evnTable.*,
       orgTracker.id               AS orgunit_id,
       insTracker.instance_id
FROM %s enrTable
       LEFT JOIN %s evnTable ON evnTable.patient_identifier = enrTable.patient_identifier AND
                                                      evnTable.enrollment_date = enrTable.enrollment_date
       INNER JOIN instance_tracker insTracker ON insTracker.patient_id = enrTable.patient_identifier
       INNER JOIN orgunit_tracker orgTracker ON orgTracker.orgUnit = enrTable.org_unit
       LEFT JOIN enrollment_tracker enrTracker
         ON enrTable.program = enrTracker.program AND enrTracker.instance_id = insTracker.instance_id
              AND enrTracker.program_unique_id = enrTable.program_unique_id :: text
WHERE enrTable.date_created :: TIMESTAMP > COALESCE((SELECT last_synced_date FROM marker WHERE category = 'enrollment'
                                                                                           AND program_name = '%s'),
                                                    '-infinity')
  AND enrTracker.instance_id IS NULL
  AND (enrTable.status = 'COMPLETED' OR enrTable.status = 'CANCELLED');
