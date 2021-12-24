SELECT
  COALESCE(enrollmentsTable.program, eventsTable.program)   AS program,
  insTracker.instance_id
FROM (SELECT enrTable.*
      FROM %s enrTable
      INNER JOIN marker enrollment_marker
          ON enrTable.date_created :: TIMESTAMP > COALESCE(enrollment_marker.last_synced_date, '-infinity')
          AND category = 'enrollment' AND program_name = '%s'
     ) AS enrollmentsTable
FULL OUTER JOIN (SELECT evnTable.*,
                   enrollments.program_unique_id AS event_program_unique_id
                   FROM %s evnTable
                   INNER JOIN %s enrollments ON evnTable.patient_identifier = enrollments.patient_identifier
                              AND evnTable.enrollment_date = COALESCE(enrollments.enrollment_date, evnTable.enrollment_date)
                   INNER JOIN marker event_marker
                       ON evnTable.date_created :: TIMESTAMP > COALESCE(event_marker.last_synced_date, '-infinity')
                       AND category = 'event' AND program_name = '%s'
                ) AS eventsTable
    ON enrollmentsTable.patient_identifier = eventsTable.patient_identifier
    AND eventsTable.enrollment_date = COALESCE(enrollmentsTable.enrollment_date, eventsTable.enrollment_date)
INNER JOIN instance_tracker insTracker ON COALESCE(eventsTable.patient_identifier, enrollmentsTable.patient_identifier) = insTracker.patient_id
LEFT JOIN enrollment_tracker enrolTracker ON COALESCE(enrollmentsTable.program, eventsTable.program) = enrolTracker.program
                                          AND enrolTracker.instance_id = insTracker.instance_id
                                          AND enrolTracker.program_unique_id = COALESCE(enrollmentsTable.program_unique_id, eventsTable.event_program_unique_id) :: TEXT
