<?php
/**
 * Validator Utility
 * 
 * Validates input data against defined rules
 */
class Validator {
    private $errors = [];
    
    /**
     * Validate data against rules
     * 
     * @param array $data Data to validate
     * @param array $rules Validation rules
     * @return bool True if validation passes, false otherwise
     */
    public function validate($data, $rules) {
        $this->errors = [];
        
        foreach ($rules as $field => $rule_set) {
            $rules_array = explode('|', $rule_set);
            
            foreach ($rules_array as $rule) {
                // Check if rule has parameters
                if (strpos($rule, ':') !== false) {
                    list($rule_name, $rule_params) = explode(':', $rule, 2);
                    $params = explode(',', $rule_params);
                } else {
                    $rule_name = $rule;
                    $params = [];
                }
                
                // Apply the rule
                $method = 'validate' . ucfirst($rule_name);
                
                if (method_exists($this, $method)) {
                    // Pass field, data, and any parameters to validation method
                    if (!$this->$method($field, $data, $params)) {
                        break; // Stop validation for this field
                    }
                }
            }
        }
        
        return empty($this->errors);
    }
    
    /**
     * Get validation errors
     * 
     * @return array Validation errors
     */
    public function getErrors() {
        return $this->errors;
    }
    
    /**
     * Check if a field is required
     * 
     * @param string $field Field name
     * @param array $data Data being validated
     * @param array $params Additional parameters
     * @return bool
     */
    private function validateRequired($field, $data, $params = []) {
        if (!isset($data[$field]) || $data[$field] === '') {
            $this->errors[$field][] = "The $field field is required.";
            return false;
        }
        return true;
    }
    
    /**
     * Check if a field is numeric
     * 
     * @param string $field Field name
     * @param array $data Data being validated
     * @param array $params Additional parameters
     * @return bool
     */
    private function validateNumeric($field, $data, $params = []) {
        if (isset($data[$field]) && $data[$field] !== '' && !is_numeric($data[$field])) {
            $this->errors[$field][] = "The $field field must be a number.";
            return false;
        }
        return true;
    }
    
    /**
     * Check if a field is an email
     * 
     * @param string $field Field name
     * @param array $data Data being validated
     * @param array $params Additional parameters
     * @return bool
     */
    private function validateEmail($field, $data, $params = []) {
        if (isset($data[$field]) && $data[$field] !== '' && !filter_var($data[$field], FILTER_VALIDATE_EMAIL)) {
            $this->errors[$field][] = "The $field field must be a valid email address.";
            return false;
        }
        return true;
    }
    
    /**
     * Check if a field has minimum length
     * 
     * @param string $field Field name
     * @param array $data Data being validated
     * @param array $params Additional parameters (min length)
     * @return bool
     */
    private function validateMin($field, $data, $params = []) {
        $min = (int)$params[0];
        if (isset($data[$field]) && strlen($data[$field]) < $min) {
            $this->errors[$field][] = "The $field field must be at least $min characters.";
            return false;
        }
        return true;
    }
    
    /**
     * Check if a field has maximum length
     * 
     * @param string $field Field name
     * @param array $data Data being validated
     * @param array $params Additional parameters (max length)
     * @return bool
     */
    private function validateMax($field, $data, $params = []) {
        $max = (int)$params[0];
        if (isset($data[$field]) && strlen($data[$field]) > $max) {
            $this->errors[$field][] = "The $field field may not be greater than $max characters.";
            return false;
        }
        return true;
    }
    
    /**
     * Check if a field equals another field
     * 
     * @param string $field Field name
     * @param array $data Data being validated
     * @param array $params Additional parameters (other field name)
     * @return bool
     */
    private function validateSame($field, $data, $params = []) {
        $other_field = $params[0];
        if (isset($data[$field]) && isset($data[$other_field]) && $data[$field] !== $data[$other_field]) {
            $this->errors[$field][] = "The $field field must match the $other_field field.";
            return false;
        }
        return true;
    }
    
    /**
     * Check if a field is within a list of values
     * 
     * @param string $field Field name
     * @param array $data Data being validated
     * @param array $params Additional parameters (allowed values)
     * @return bool
     */
    private function validateIn($field, $data, $params = []) {
        if (isset($data[$field]) && !in_array($data[$field], $params)) {
            $allowed = implode(', ', $params);
            $this->errors[$field][] = "The $field field must be one of: $allowed.";
            return false;
        }
        return true;
    }
}
