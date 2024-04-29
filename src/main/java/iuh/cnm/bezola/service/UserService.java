package iuh.cnm.bezola.service;

import iuh.cnm.bezola.dto.ChangePasswordDTO;
import iuh.cnm.bezola.dto.ForgotPasswordDTO;
import iuh.cnm.bezola.dto.UpdateUserDTO;
import iuh.cnm.bezola.dto.UserDto;
import iuh.cnm.bezola.exceptions.DataAlreadyExistsException;
import iuh.cnm.bezola.exceptions.DataNotFoundException;
import iuh.cnm.bezola.exceptions.UserException;
import iuh.cnm.bezola.models.PhoneBook;
import iuh.cnm.bezola.models.User;
import iuh.cnm.bezola.repository.UserRepository;
import iuh.cnm.bezola.responses.UserResponse;
import iuh.cnm.bezola.utils.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MongoTemplate mongoTemplate;
    private final JwtTokenUtil jwtTokenUtil;

    public boolean isUserExists(String phone) {
        return userRepository.existsByPhone(phone);
    }

    public User getUserByPhone(String phone) throws UserException {
        Optional<User> optionalUser = userRepository.findByPhone(phone);
        if (optionalUser.isEmpty()) {
            throw new UserException("User not found with phone: " + phone);
        }
        return optionalUser.get();
    }
    public void savePhoneBooks(String jwt, List<PhoneBook> phoneBooks) throws UserException {
        User user = findUserProfileByJwt(jwt);
        user.setPhoneBooks(phoneBooks);
        userRepository.save(user);
    }
    public List<PhoneBook> getPhoneBookByPhone(String phone) {
        Optional<User> optionalUser = userRepository.findByPhone(phone);
        return optionalUser.map(User::getPhoneBooks).orElse(null);
    }

    public User updateUser(String phone, ForgotPasswordDTO forgotPasswordDTO) throws DataNotFoundException {
        Optional<User> optionalUser = userRepository.findByPhone(phone);
        if (optionalUser.isEmpty()) {
            throw new DataNotFoundException("User not found with phone: " + phone);
        }
        User user = optionalUser.get();
        if(!forgotPasswordDTO.getConfirmPassword().equals(forgotPasswordDTO.getPassword())) {
            throw new DataNotFoundException("Password not match");
        }
        user.setPassword(passwordEncoder.encode(forgotPasswordDTO.getPassword()));
        return userRepository.save(user);
    }

    public User changePassword(String phone, ChangePasswordDTO changePasswordDTO) throws DataNotFoundException {
        Optional<User> optionalUser = userRepository.findByPhone(phone);
        if (optionalUser.isEmpty()) {
            throw new DataNotFoundException("User not found with phone: " + phone);
        }
        User user = optionalUser.get();
        if(!passwordEncoder.matches(changePasswordDTO.getOldPassword(),user.getPassword())) {
            throw new DataNotFoundException("Old password not match");
        }
        if(!changePasswordDTO.getConfirmPassword().equals(changePasswordDTO.getNewPassword())) {
            throw new DataNotFoundException("Password not match");
        }
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        return userRepository.save(user);
    }

    public User changePasswordById(String id, ChangePasswordDTO changePasswordDTO) throws DataNotFoundException {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new DataNotFoundException("User not found with phone: " + id);
        }
        User user = optionalUser.get();
        if(!passwordEncoder.matches(changePasswordDTO.getOldPassword(),user.getPassword())) {
            throw new DataNotFoundException("Old password not match");
        }
        if(!changePasswordDTO.getConfirmPassword().equals(changePasswordDTO.getNewPassword())) {
            throw new DataNotFoundException("Password not match");
        }
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        return userRepository.save(user);
    }


    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void addFriend(String id, String friendId) throws DataNotFoundException, DataAlreadyExistsException {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new DataNotFoundException("User not found with id: " + id);
        }

        // Check if the friend ID exists
        Optional<User> optionalFriend = userRepository.findById(friendId);
        if (optionalFriend.isEmpty()) {
            throw new DataNotFoundException("Friend not found with id: " + friendId);
        }

        User user = optionalUser.get();

        if (user.getFriends() == null) {
            user.setFriends(new ArrayList<>());
        }

        if (!user.getFriends().contains(friendId)) {
            user.getFriends().add(friendId);

            userRepository.save(user);
        } else {
            throw new DataAlreadyExistsException("Friend already added with id: " + friendId);
        }
    }

    public List<User> getFriendByName(String userId, String name) throws DataNotFoundException {
        // Tìm kiếm user bằng id
        User user = mongoTemplate.findById(userId, User.class);
        if (user == null) {
            throw new DataNotFoundException("User not found with id: " + userId);
        }

        // Chuẩn hóa tên tìm kiếm để không phân biệt dấu
        String normalizedSearchName = removeAccents(name).toLowerCase();
        // Tạo truy vấn để tìm bạn bè dựa trên danh sách friends của user và so khớp tên
        Query query = new Query();

        // Tạo điều kiện lọc id trong danh sách bạn bè
        Criteria idCriteria = Criteria.where("id").in(user.getFriends());
        // Tạo điều kiện so khớp tên
        Criteria nameCriteria = Criteria.where("name").regex(normalizedSearchName, "i");

        // Kết hợp các điều kiện sử dụng andOperator
        query.addCriteria(new Criteria().andOperator(idCriteria, nameCriteria));

        return mongoTemplate.find(query, User.class);
    }

    public static String removeAccents(String text) {
        String nfdNormalizedString = Normalizer.normalize(text, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

    public User getUserById(String id) throws DataNotFoundException {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new DataNotFoundException("User not found with id: " + id);
        }
        return optionalUser.get();
    }

    public void updateAvatarUser(String id, String avatar) throws DataNotFoundException {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new DataNotFoundException("User not found with id: " + id);
        }
        User user = optionalUser.get();
        user.setAvatar(avatar);
        userRepository.save(user);
    }

    public void updateImageCoverUser(String id, String imageUrl) throws DataNotFoundException {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new DataNotFoundException("User not found with id: " + id);
        }
        User user = optionalUser.get();
        user.setImageCover(imageUrl);
        userRepository.save(user);
    }

    public User update(String phone, UpdateUserDTO updateUserDTO) throws DataNotFoundException {
        Optional<User> optionalUser = userRepository.findByPhone(phone);
        if (optionalUser.isEmpty()) {
            throw new DataNotFoundException("User not found with phone: " + phone);
        }
        User user = optionalUser.get();
        if(updateUserDTO.getName() != null && !updateUserDTO.getName().isEmpty()) {
            user.setName(updateUserDTO.getName());
        }
        if(updateUserDTO.getBirthday() != null) {
            user.setBirthday(updateUserDTO.getBirthday());
        }
        user.setSex(updateUserDTO.isSex());
        return userRepository.save(user);
    }

    public User findUserProfileByJwt(String jwt) throws UserException {
        jwt = jwt.substring(7);
        String phone = jwtTokenUtil.extractPhone(jwt);
        User user = getUserByPhone(phone);
        if(user == null){
            throw new UserException("User not found with phone: " + phone);
        }
        return user;
    }
    private String getNameFromPhoneBooks(List<PhoneBook> phoneBooks, String phone,String name) {
        if (phoneBooks == null) {
            return name;
        }
        for (PhoneBook phoneBook : phoneBooks) {
            if (phoneBook.getPhone().equals(phone)) {
                return phoneBook.getName();
            }
        }
        return name;
    }

    public List<UserResponse> getFriends(String jwt) throws UserException {
        User user = findUserProfileByJwt(jwt);
        if(user.getFriends()==null){
            return new ArrayList<>();
        }
        List<UserResponse> userResponses = new ArrayList<>();
        List<PhoneBook> phoneBooks = user.getPhoneBooks();
        for (String friendId : user.getFriends()) {
            User friend = userRepository.findById(friendId).orElse(null);
            if(friend!=null){
                userResponses.add(UserResponse.builder()
                .id(friend.getId())
                .name(getNameFromPhoneBooks(phoneBooks, friend.getPhone(), friend.getName()))
                .phone(friend.getPhone())
                .avatar(friend.getAvatar()).build());
            }
        }
        return userResponses;
    }

    public List<UserResponse> getFriendsFromPhoneBook(String jwt) throws UserException {
        User user = findUserProfileByJwt(jwt);
        List<UserResponse> userResponses = new ArrayList<>();
        List<PhoneBook> phoneBooks = user.getPhoneBooks();
        if(phoneBooks==null){
            return new ArrayList<>();
        }
        for (PhoneBook phoneBook : phoneBooks) {
            User friend = userRepository.findByPhone(phoneBook.getPhone()).orElse(null);
            if(friend!=null){
                userResponses.add(UserResponse.builder()
                .id(friend.getId())
                .name(getNameFromPhoneBooks(phoneBooks, friend.getPhone(), friend.getName()))
                .phone(friend.getPhone())
                .avatar(friend.getAvatar()).build());
            }
        }
        return userResponses;
    }

    public User getSkipMe(String phone, String id) throws UserException {

        Optional<User> optionalUser = userRepository.findByPhone(phone);

        if (optionalUser.isEmpty()) {
            throw new UserException("User not found with phone: " + phone);
        }

        if (optionalUser.get().getId().equals(id)) {
            throw new UserException("This is your account! You can't skip yourself!");
        }

        return optionalUser.get();
    }

    //findById
    public User findById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    public User saveToken(String jwt, String token) throws UserException {
        User user = findUserProfileByJwt(jwt);
        List<String> tokens = user.getTokens();
        if(tokens == null){
            tokens = new ArrayList<>();
        } else {
            if(tokens.contains(token)){
                return user;
            }
        }

        tokens.add(token);
        user.setTokens(tokens);
        return userRepository.save(user);
    }

    public List<String> findTokensByUserId(String id) {
        User user = userRepository.findById(id).orElse(null);
        if(user == null){
            return null;
        }
        return user.getTokens();
    }
}
