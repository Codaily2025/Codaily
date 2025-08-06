import useFormStore from "../store/formStore"

const useFormField = (fieldName) => {
    const { formData, updateField } = useFormStore()

    return {
        value: formData[fieldName],
        onChange: (e) => updateField(fieldName, e.target.value)
    }
}

export default useFormField