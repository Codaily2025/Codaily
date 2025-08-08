import React from 'react'
import FormRow from '@/components/molecules/FormRow'
import InputGroup from '@/components/molecules/InputGroup'

const AddressSection = ({ 
    className = '', 
    titleClassName = '',
    rowClassName = '',
    groupClassName = '',
    labelClassName = '',
    inputClassName = '',
    formData,
    updateField
}) => {
    return (
        <div className={className}>
            <h3 className={titleClassName}>Personal Address</h3>
            
            <FormRow className={rowClassName}>
                <InputGroup 
                    label="Country"
                    fieldName="country"
                    placeholder="Country"
                    value={formData.country}
                    onChange={(e) => updateField('country', e.target.value)}
                    className={groupClassName}
                    labelClassName={labelClassName}
                    inputClassName={inputClassName}
                />
                <InputGroup 
                    label="City"
                    fieldName="city"
                    placeholder="City"
                    value={formData.city}
                    onChange={(e) => updateField('city', e.target.value)}
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
                    value={formData.address}
                    onChange={(e) => updateField('address', e.target.value)}
                    className={groupClassName}
                    labelClassName={labelClassName}
                    inputClassName={inputClassName}
                />
                <InputGroup 
                    label="Zip Code"
                    fieldName="zipCode"
                    placeholder="Zip Code"
                    value={formData.zipCode}
                    onChange={(e) => updateField('zipCode', e.target.value)}
                    className={groupClassName}
                    labelClassName={labelClassName}
                    inputClassName={inputClassName}
                />
            </FormRow>
        </div>
    )
}

export default AddressSection