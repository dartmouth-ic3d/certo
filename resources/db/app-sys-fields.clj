{:schema_name "app" :table_name "options_states" :field_name "id" :type "serial8" :is_pk true :label "ID" :control "text" :location 0 :in_table_view true :disabled false :readonly true :required false :text_max_length nil :boolean_true nil :boolean_false nil :date_min nil  :date_max nil :integer_step nil :integer_min nil :integer_max nil :float_step nil :float_min nil :float_max nil :select_multiple nil :select_size nil :options_schema_table nil :created_by "root" :updated_by "root"}

{:schema_name "app" :table_name "options_states" :field_name "label" :type "text" :is_pk false :label "Label" :control "text" :location 1 :in_table_view true :disabled false :readonly false :required true :text_max_length 25 :boolean_true nil :boolean_false nil :date_min nil  :date_max nil :integer_step nil :integer_min nil :integer_max nil :float_step nil :float_min nil :float_max nil :select_multiple nil :select_size nil :options_schema_table nil :created_by "root" :updated_by "root"}

{:schema_name "app" :table_name "options_states" :field_name "value" :type "text" :is_pk false :label "Value" :control "text" :location 2 :in_table_view true :disabled false :readonly false :required true :text_max_length 25 :boolean_true nil :boolean_false nil :date_min nil  :date_max nil :integer_step nil :integer_min nil :integer_max nil :float_step nil :float_min nil :float_max nil :select_multiple nil :select_size nil :options_schema_table nil :created_by "root" :updated_by "root"}

{:schema_name "app" :table_name "options_states" :field_name "location" :type "int8" :is_pk false :label "Location" :control "integer" :location 8 :in_table_view true :disabled false :readonly false :required true :text_max_length nil :boolean_true nil :boolean_false nil :date_min nil  :date_max nil :integer_step 1 :integer_min 0 :integer_max nil :float_step nil :float_min nil :float_max nil :select_multiple nil :select_size nil :options_schema_table nil :created_by "root" :updated_by "root"}



{:schema_name "study" :table_name "subjects" :field_name "id" :type "serial8" :is_pk true :label "ID" :control "text" :location 0 :in_table_view true :disabled false :readonly true :required false :text_max_length nil :boolean_true nil :boolean_false nil :date_min nil  :date_max nil :integer_step nil :integer_min nil :integer_max nil :float_step nil :float_min nil :float_max nil :select_multiple nil :select_size nil :options_schema_table nil :created_by "root" :updated_by "root"}

{:schema_name "study" :table_name "subjects" :field_name "first_name" :type "text" :is_pk false :label "First Name" :control "text" :location 1 :in_table_view true :disabled false :readonly false :required true :text_max_length 25 :boolean_true nil :boolean_false nil :date_min nil  :date_max nil :integer_step nil :integer_min nil :integer_max nil :float_step nil :float_min nil :float_max nil :select_multiple nil :select_size nil :options_schema_table nil :created_by "root" :updated_by "root"}

{:schema_name "study" :table_name "subjects" :field_name "last_name" :type "text" :is_pk false :label "Last Name" :control "text" :location 2 :in_table_view true :disabled false :readonly false :required true :text_max_length 25 :boolean_true nil :boolean_false nil :date_min nil  :date_max nil :integer_step nil :integer_min nil :integer_max nil :float_step nil :float_min nil :float_max nil :select_multiple nil :select_size nil :options_schema_table nil :created_by "root" :updated_by "root"}

{:schema_name "study" :table_name "subjects" :field_name "birth_date" :type "date" :is_pk false :label "Birth Date" :control "date" :location 3 :in_table_view true :disabled false :readonly false :required false :text_max_length nil :boolean_true nil :boolean_false nil :date_min "1700-01-01" :date_max "2025-12-31" :integer_step nil :integer_min nil :integer_max nil :float_step nil :float_min nil :float_max nil :select_multiple nil :select_size nil :options_schema_table nil :created_by "root" :updated_by "root"}

{:schema_name "study" :table_name "subjects" :field_name "birth_state" :type "text" :is_pk false :label "Birth State" :control "select" :location 4 :in_table_view false :disabled false :readonly false :required false :text_max_length nil :boolean_true nil :boolean_false nil :date_min nil  :date_max nil :integer_step nil :integer_min nil :integer_max nil :float_step nil :float_min nil :float_max nil :select_multiple false :select_size 1 :options_schema_table "app.options_states" :created_by "root" :updated_by "root"}

{:schema_name "study" :table_name "subjects" :field_name "children" :type "boolean" :is_pk false :label "Children" :control "boolean-select" :location 5 :in_table_view false :disabled false :readonly false :required false :text_max_length nil :boolean_true "Yes" :boolean_false "No" :date_min nil :date_max nil :integer_step nil :integer_min nil :integer_max nil :float_step nil :float_min nil :float_max nil :select_multiple nil :select_size nil :options_schema_table nil :created_by "root" :updated_by "root"}

