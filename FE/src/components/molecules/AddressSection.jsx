import React from 'react'
import FormRow from '@/components/molecules/FormRow'
import InputGroup from '@/components/molecules/InputGroup'

const AddressSection = ({ 
    className = '', 
    titleClassName = '',
    rowClassName = '',
    groupClassName = '',
    labelClassName = '',
    inputClassName = ''
}) => {
    return (
        <div className={className}>
            <h3 className={titleClassName}>Personal Address</h3>
            
            <FormRow className={rowClassName}>
                <InputGroup 
                    label="Country"
                    fieldName="country"
                    placeholder="Country"
                    className={groupClassName}
                    labelClassName={labelClassName}
                    inputClassName={inputClassName}
                />
                <InputGroup 
                    label="City"
                    fieldName="city"
                    placeholder="City"
                    className={groupClassName}
                    labelClassName={labelClassName}
                    inputClassName={inputClassName}
                />
            </FormRow>

            <FormRow className={rowClassName}>
                <InputGroup 
                    label="Address"
                    fieldName="address"
                    placeholder="Address"
                    className={groupClassName}
                    labelClassName={labelClassName}
                    inputClassName={inputClassName}
                />
                <InputGroup 
                    label="Zip Code"
                    fieldName="zipCode"
                    placeholder="Zip Code"
                    className={groupClassName}
                    labelClassName={labelClassName}
                    inputClassName={inputClassName}
                />
            </FormRow>
        </div>
    )
}

export default AddressSection