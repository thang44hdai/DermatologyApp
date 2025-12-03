package com.example.safeaid.core.data

import com.example.safeaid.core.response.BrandResponse
import com.example.safeaid.core.response.MedicineResponse
import com.example.safeaid.core.response.PharmacyResponse

object MockDataSource {
    
    fun getMockMedicines(): List<MedicineResponse> {
        return listOf(
            MedicineResponse(
                id = "1",
                name = "Kem dưỡng ẩm La Roche-Posay",
                genericName = "Hydrating Cream",
                description = "Kem dưỡng ẩm chuyên sâu cho da khô và nhạy cảm. Công thức không gây kích ứng, phù hợp cho mọi loại da.",
                price = "450.000đ",
                images = listOf(
                    "https://images.unsplash.com/photo-1556228578-0d85b1a4d571?w=400",
                    "https://images.unsplash.com/photo-1556228720-195a672e8a03?w=400"
                ),
                brand = BrandResponse(
                    id = "b1",
                    name = "La Roche-Posay",
                    description = "Thương hiệu dược mỹ phẩm hàng đầu từ Pháp",
                    logoPath = "https://images.unsplash.com/photo-1599305445671-ac291c95aaa9?w=200"
                ),
                type = "Kem dưỡng",
                dosage = "Thoa 2 lần/ngày",
                suitableFor = "Da khô, da nhạy cảm",
                sideEffects = "Hiếm khi gây kích ứng",
                diseaseIds = listOf("dry_skin", "sensitive_skin")
            ),
            MedicineResponse(
                id = "2",
                name = "Serum Vitamin C Klairs",
                genericName = "Vitamin C Serum",
                description = "Serum vitamin C giúp làm sáng da, mờ thâm nám và chống lão hóa hiệu quả.",
                price = "380.000đ",
                images = listOf(
                    "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=400"
                ),
                brand = BrandResponse(
                    id = "b2",
                    name = "Klairs",
                    description = "Thương hiệu mỹ phẩm thuần chay từ Hàn Quốc",
                    logoPath = "https://images.unsplash.com/photo-1599305445671-ac291c95aaa9?w=200"
                ),
                type = "Serum",
                dosage = "Sử dụng buổi tối",
                suitableFor = "Mọi loại da",
                sideEffects = "Có thể gây kích ứng nhẹ ở da nhạy cảm",
                diseaseIds = listOf("dark_spots", "aging")
            ),
            MedicineResponse(
                id = "3",
                name = "Sữa rửa mặt CeraVe",
                genericName = "Foaming Facial Cleanser",
                description = "Sữa rửa mặt dạng gel tạo bọt nhẹ nhàng, làm sạch sâu mà không làm khô da.",
                price = "280.000đ",
                images = listOf(
                    "https://images.unsplash.com/photo-1556228578-8c89e6adf883?w=400"
                ),
                brand = BrandResponse(
                    id = "b3",
                    name = "CeraVe",
                    description = "Thương hiệu được bác sĩ da liễu khuyên dùng",
                    logoPath = "https://images.unsplash.com/photo-1599305445671-ac291c95aaa9?w=200"
                ),
                type = "Sữa rửa mặt",
                dosage = "Sử dụng 2 lần/ngày",
                suitableFor = "Da thường, da hỗn hợp",
                sideEffects = "Không",
                diseaseIds = listOf("acne", "oily_skin")
            ),
            MedicineResponse(
                id = "4",
                name = "Kem chống nắng Anessa",
                genericName = "Sunscreen SPF 50+",
                description = "Kem chống nắng bảo vệ da khỏi tia UV, chống nước và mồ hôi hiệu quả.",
                price = "520.000đ",
                images = listOf(
                    "https://images.unsplash.com/photo-1556228720-195a672e8a03?w=400"
                ),
                brand = BrandResponse(
                    id = "b4",
                    name = "Anessa",
                    description = "Thương hiệu chống nắng số 1 Nhật Bản",
                    logoPath = "https://images.unsplash.com/photo-1599305445671-ac291c95aaa9?w=200"
                ),
                type = "Kem chống nắng",
                dosage = "Thoa trước khi ra ngoài 15-30 phút",
                suitableFor = "Mọi loại da",
                sideEffects = "Không",
                diseaseIds = listOf("sun_protection")
            ),
            MedicineResponse(
                id = "5",
                name = "Mặt nạ Some By Mi",
                genericName = "Tea Tree Mask",
                description = "Mặt nạ giấy chiết xuất tràm trà giúp làm dịu da, giảm mụn và kiểm soát dầu.",
                price = "35.000đ",
                images = listOf(
                    "https://images.unsplash.com/photo-1608248543803-ba4f8c70ae0b?w=400"
                ),
                brand = BrandResponse(
                    id = "b5",
                    name = "Some By Mi",
                    description = "Thương hiệu chăm sóc da từ Hàn Quốc",
                    logoPath = "https://images.unsplash.com/photo-1599305445671-ac291c95aaa9?w=200"
                ),
                type = "Mặt nạ",
                dosage = "Sử dụng 2-3 lần/tuần",
                suitableFor = "Da mụn, da dầu",
                sideEffects = "Không",
                diseaseIds = listOf("acne", "oily_skin")
            ),
            MedicineResponse(
                id = "6",
                name = "Toner Hada Labo",
                genericName = "Hydrating Lotion",
                description = "Nước hoa hồng cấp ẩm chuyên sâu với hyaluronic acid, giúp da mềm mại và căng mịn.",
                price = "195.000đ",
                images = listOf(
                    "https://images.unsplash.com/photo-1571875257727-256c39da42af?w=400"
                ),
                brand = BrandResponse(
                    id = "b6",
                    name = "Hada Labo",
                    description = "Thương hiệu dưỡng da từ Nhật Bản",
                    logoPath = "https://images.unsplash.com/photo-1599305445671-ac291c95aaa9?w=200"
                ),
                type = "Toner",
                dosage = "Sử dụng sau khi rửa mặt",
                suitableFor = "Da khô, da thường",
                sideEffects = "Không",
                diseaseIds = listOf("dry_skin")
            ),
            MedicineResponse(
                id = "7",
                name = "Gel trị mụn Acnes",
                genericName = "Acne Treatment Gel",
                description = "Gel điều trị mụn với công thức kháng khuẩn, giúp giảm viêm và làm khô mụn nhanh chóng.",
                price = "85.000đ",
                images = listOf(
                    "https://images.unsplash.com/photo-1556228578-8c89e6adf883?w=400"
                ),
                brand = BrandResponse(
                    id = "b7",
                    name = "Acnes",
                    description = "Thương hiệu chuyên trị mụn từ Nhật Bản",
                    logoPath = "https://images.unsplash.com/photo-1599305445671-ac291c95aaa9?w=200"
                ),
                type = "Gel trị mụn",
                dosage = "Thoa lên vùng da bị mụn 2-3 lần/ngày",
                suitableFor = "Da mụn",
                sideEffects = "Có thể gây khô da",
                diseaseIds = listOf("acne")
            ),
            MedicineResponse(
                id = "8",
                name = "Kem dưỡng mắt Innisfree",
                genericName = "Eye Cream",
                description = "Kem dưỡng vùng mắt giúp giảm quầng thâm, bọng mắt và nếp nhăn.",
                price = "320.000đ",
                images = listOf(
                    "https://images.unsplash.com/photo-1556228578-0d85b1a4d571?w=400"
                ),
                brand = BrandResponse(
                    id = "b8",
                    name = "Innisfree",
                    description = "Thương hiệu mỹ phẩm thiên nhiên từ Hàn Quốc",
                    logoPath = "https://images.unsplash.com/photo-1599305445671-ac291c95aaa9?w=200"
                ),
                type = "Kem dưỡng mắt",
                dosage = "Thoa nhẹ nhàng quanh vùng mắt buổi sáng và tối",
                suitableFor = "Mọi loại da",
                sideEffects = "Không",
                diseaseIds = listOf("dark_circles", "aging")
            ),
            MedicineResponse(
                id = "9",
                name = "Tinh chất Estée Lauder",
                genericName = "Advanced Night Repair",
                description = "Tinh chất phục hồi da ban đêm, giúp da tái tạo và chống lão hóa hiệu quả.",
                price = "2.850.000đ",
                images = listOf(
                    "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=400"
                ),
                brand = BrandResponse(
                    id = "b9",
                    name = "Estée Lauder",
                    description = "Thương hiệu mỹ phẩm cao cấp từ Mỹ",
                    logoPath = "https://images.unsplash.com/photo-1599305445671-ac291c95aaa9?w=200"
                ),
                type = "Tinh chất",
                dosage = "Sử dụng buổi tối sau toner",
                suitableFor = "Mọi loại da",
                sideEffects = "Không",
                diseaseIds = listOf("aging", "dull_skin")
            ),
            MedicineResponse(
                id = "10",
                name = "Xịt khoáng Avène",
                genericName = "Thermal Spring Water",
                description = "Xịt khoáng làm dịu da, cân bằng độ pH và cấp ẩm tức thì cho da.",
                price = "285.000đ",
                images = listOf(
                    "https://images.unsplash.com/photo-1571875257727-256c39da42af?w=400"
                ),
                brand = BrandResponse(
                    id = "b10",
                    name = "Avène",
                    description = "Thương hiệu dược mỹ phẩm từ Pháp",
                    logoPath = "https://images.unsplash.com/photo-1599305445671-ac291c95aaa9?w=200"
                ),
                type = "Xịt khoáng",
                dosage = "Xịt lên da khi cần thiết",
                suitableFor = "Da nhạy cảm, mọi loại da",
                sideEffects = "Không",
                diseaseIds = listOf("sensitive_skin", "irritated_skin")
            ),
            MedicineResponse(
                id = "11",
                name = "Kem trị thâm Melano CC",
                genericName = "Vitamin C Spot Treatment",
                description = "Kem điểm trị thâm nám, vết thâm mụn với vitamin C nguyên chất.",
                price = "165.000đ",
                images = listOf(
                    "https://images.unsplash.com/photo-1556228578-8c89e6adf883?w=400"
                ),
                brand = BrandResponse(
                    id = "b11",
                    name = "Melano CC",
                    description = "Thương hiệu chuyên trị thâm từ Nhật Bản",
                    logoPath = "https://images.unsplash.com/photo-1599305445671-ac291c95aaa9?w=200"
                ),
                type = "Kem điểm trị",
                dosage = "Thoa lên vùng da thâm 1-2 lần/ngày",
                suitableFor = "Da có vết thâm",
                sideEffects = "Có thể gây kích ứng nhẹ",
                diseaseIds = listOf("dark_spots", "acne_scars")
            ),
            MedicineResponse(
                id = "12",
                name = "Sữa dưỡng thể Vaseline",
                genericName = "Body Lotion",
                description = "Sữa dưỡng thể cấp ẩm chuyên sâu, giúp da mềm mại và không khô ráp.",
                price = "125.000đ",
                images = listOf(
                    "https://images.unsplash.com/photo-1556228720-195a672e8a03?w=400"
                ),
                brand = BrandResponse(
                    id = "b12",
                    name = "Vaseline",
                    description = "Thương hiệu chăm sóc da toàn thân",
                    logoPath = "https://images.unsplash.com/photo-1599305445671-ac291c95aaa9?w=200"
                ),
                type = "Sữa dưỡng thể",
                dosage = "Thoa lên toàn thân sau khi tắm",
                suitableFor = "Mọi loại da",
                sideEffects = "Không",
                diseaseIds = listOf("dry_skin")
            )
        )
    }
    
    fun getMockPharmacies(): List<PharmacyResponse> {
        return listOf(
            PharmacyResponse(
                id = "p1",
                name = "Nhà thuốc Guardian",
                address = "123 Nguyễn Huệ, Quận 1, TP.HCM",
                phone = "0281234567",
                ratings = "4.5",
                distanceKm = "0.5",
                latitude = 10.7769,
                longitude = 106.7009,
                openTime = "07:00",
                closeTime = "22:00",
                images = listOf("https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?w=400"),
                logoUrl = "https://images.unsplash.com/photo-1599305445671-ac291c95aaa9?w=200"
            ),
            PharmacyResponse(
                id = "p2",
                name = "Pharmacity",
                address = "456 Lê Lợi, Quận 1, TP.HCM",
                phone = "0287654321",
                ratings = "4.7",
                distanceKm = "0.8",
                latitude = 10.7740,
                longitude = 106.6990,
                openTime = "00:00",
                closeTime = "23:59",
                images = listOf("https://images.unsplash.com/photo-1576091160550-2173dba999ef?w=400"),
                logoUrl = "https://images.unsplash.com/photo-1599305445671-ac291c95aaa9?w=200"
            ),
            PharmacyResponse(
                id = "p3",
                name = "Nhà thuốc Long Châu",
                address = "789 Trần Hưng Đạo, Quận 5, TP.HCM",
                phone = "0289876543",
                ratings = "4.6",
                distanceKm = "1.2",
                latitude = 10.7545,
                longitude = 106.6760,
                openTime = "06:00",
                closeTime = "23:00",
                images = listOf("https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?w=400"),
                logoUrl = "https://images.unsplash.com/photo-1599305445671-ac291c95aaa9?w=200"
            )
        )
    }
}
