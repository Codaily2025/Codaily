import React from "react"
import SaveButton from '../atoms/SaveButton'
import NicknameCheckButton from '../atoms/NicknameCheckButton'
import useModalStore from "../../store/modalStore"

{/* TODO : 컴포넌트 분리하기 */}
const AdditionalInfo = () => {
    const { openModal } = useModalStore()


    {/* TODO: 닉네임 중복 여부 따라 분기 처리하기 */}
    const handleNicknameCheck = () => {
        openModal('NICKNAME_CHECK', {
            nickname: 'user_nickname'   // 현재 입력된 닉네임값 전달
        })
    }
    return (
            <div class="container">
                <div class="main-content">

                    <div class="profile-section">
                        <div class="profile-avatar-section">
                            <div class="profile-avatar">
                                <div class="camera-icon">📷</div>
                            </div>
                        </div>

                        <form>
                            <div class="form-row">
                                <div class="form-group">
                                    <label class="form-label">First Name</label>
                                    <input type="text" class="form-input" value="Yoshikage" placeholder="Yoshikage" />
                                </div>
                                <div class="form-group">
                                    <label class="form-label">First Name</label>
                                    <input type="text" class="form-input" value="Kira" placeholder="Kira" />
                                </div>
                            </div>

                            <div class="form-row">
                                <div class="form-group full-width">
                                    <label class="form-label">Email Address</label>
                                    <input type="email" class="form-input" value="YoshikageKira@gmail.com" placeholder="YoshikageKira@gmail.com" />
                                </div>
                            </div>

                            <div class="form-row">
                                <div class="form-group">
                                    <label class="form-label">Nickname</label>
                                    <input type="text" class="form-input" value="user_nickname" placeholder="user_nickname" />
                                </div>
                                <NicknameCheckButton onClick={handleNicknameCheck} />
                            </div>

                            <div class="address-section">
                                <h3 class="section-title">Personal Address</h3>

                                <div class="form-row">
                                    <div class="form-group">
                                        <label class="form-label">Country</label>
                                        <input type="text" class="form-input" value="Vietnam" placeholder="Vietnam" />
                                    </div>
                                    <div class="form-group">
                                        <label class="form-label">City</label>
                                        <input type="text" class="form-input" value="Hai Phong" placeholder="Hai Phong" />
                                    </div>
                                </div>

                                <div class="form-row">
                                    <div class="form-group">
                                        <label class="form-label">Address</label>
                                        <input type="text" class="form-input" value="Hong Bang" placeholder="Hong Bang" />
                                    </div>
                                    <div class="form-group">
                                        <label class="form-label">Zip Code</label>
                                        <input type="text" class="form-input" value="180000" placeholder="180000" />
                                    </div>
                                </div>
                            </div>

                            <div class="form-actions">
                                {/* <button type="submit" class="save-button">Save Changes</button> */}
                                <SaveButton />
                            </div>
                        </form>
                    </div>
                </div>
            </div>
    )
}

export default AdditionalInfo