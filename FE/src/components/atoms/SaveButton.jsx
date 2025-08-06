import useFormStore from "@/store/formStore"

const SaveButton = () => {
  const { handleSave } = useFormStore()

  return (
    <button
      onClick={handleSave}
      style={{
        backgroundColor: '#5A597D',
        color: '#fff',
        border: 'none',
        borderRadius: '12px',
        padding: '8px 16px',
        cursor: 'pointer',
        marginTop: '16px',
      }}
    >
      저장
    </button>
  )
}

export default SaveButton